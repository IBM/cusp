# CUSP: ConcUrrent Staged Pipelines

CUSP is a framework for constructing and executing pipelines. It represents a pipeline as a directed graph with a single source and sink, constructed using [JGraphT](https://jgrapht.org/), executed using [ParSeq](https://github.com/linkedin/parseq), and visualized using tools from both of those projects.


## Usage

### Declaring pipeline stage identifiers

Create a new interface extending `Stages` that defines string constants; these will be unique identifiers for the stages in your pipeline. If you try to instantiate a pipeline with two stages that have the same name, CUSP will throw a `StageAlreadyExistsException`:
```
import com.ibm.cusp.graph.stages.Stages;

public interface WidgetStages extends Stages {
    String RECEIVE_REQUEST = "receiveRequest";
    String PARSE_REQUEST = "parseRequest";
    String PLACE_ORDER = "placeOrder";
    String SEND_EMAIL = "sendEmail";
    String LOG_REQUEST = "logRequest";
    String QUERY_INVENTORY = "queryInventory";
    String QUERY_BACKUP_SYSTEM = "queryBackupSystem";
    String MANUFACTURE_WIDGETS = "manufactureWidgets";
}
```


### Implementing stages

Create a new class extending and implementing `AbstractStage` for each pipeline stage, e.g.:
```
import com.ibm.cusp.graph.stages.AbstractStage;

class ParseQueryStage extends AbstractStage<String, String> {
    @Override
    public String name() {
        return PluginsStages.PARSE_QUERY;
    }

    @Override
    public String execute(String input) throws Exception {
        logger.logInfo("parsing query {0}", input);
        return MyCoolParsingLibrary.parse(input);
    }
}
```


### Registering stages with CUSP

Next, we start declaring a CUSP pipeline by instantiating a `Cusp` object and registering our stages with that object so that it can do bookkeeping and constraint checking at compile time and runtime, e.g.:
```
        Cusp Cusp = new Cusp();

        cusp.addStage(new WidgetPurchaseRequestStage());
        cusp.addStage(parseRequestStage);
        cusp.addStage(new SendEmailStage(sink));

```

Each stage will be internally registered in CUSP as identified by its `name()` method. Going forward, stages are referred to only using their identifiers, as mentioned above.


### Connecting the stages into a pipeline

The output of one stage, when successful, can be routed to another stage using what CUSP calls "routes", e.g.:
```
        cusp.addRoute(WidgetStages.RECEIVE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.PARSE_REQUEST);
        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.SEND_EMAIL);```
```

The outcome of a stage can also be `StageOutcomes.RECOVERABLE_FAILURE` if the stage in question fails, allowing you to define another stage that is able to recover from that failure. For total failures, see _Error handling_ below.

### Concurrent Stages

CUSP is able to execute any number of stages concurrently, with one caveat: only one of them can be used to supply the input for downstream stages. In other words, CUSP assumes that there is exactly one critical execution path in your application. This means that the other concurrent stages that are "leaf" stages alongside your critical execution path stage should either modify a shared object to be used by other stages (not recommended: see "God Object") or have a side effect that is not used in other stages. If you do not follow this model, an exception will be thrown that tells you where the problem is.

Defining multiple stages for concurrent execution is  accomplished by attaching multiple stages as children of one parent stage, such as :
```
        cusp.addRoute(WidgetStages.RECEIVE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.PARSE_REQUEST);
        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.SEND_EMAIL);
        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.LOG_REQUEST);
        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.QUERY_INVENTORY);
```

### Executing the pipeline

Once the pipeline is constructed, it can be executed using `CuspExecutor`, which can optionally be passed two thread pools, one not scheduled (e.g. `Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())` and one scheduled (e.g. `Executors.newSingleThreadScheduledExecutor()`:

```
CuspExecutor executor = new CuspExecutor(Cusp, taskExecutor, timerScheduler);
```

_Optionally, `CuspExecutor` can be constructed only from a `Cusp` object with executors provided later via `CuspExecutor#useExecutors`. This allows you to decouple the code constructing a pipeline from resource handling/management code._

Next, the executor needs to be initialized using a CUSP pipeline and a concrete input to the first stage, e.g.:
```
executor.constructPipeline(PluginsStages.RECEIVE_REQUEST, request);
```

Finally, execute:
```
Result result = executor.execute();
```

Note that `CuspExecutor#execute` is method type parameterized, so trying to assign its output to a variable of an incorrect type will compile but will fail with a `ClassCastException`. (This is a **TODO**; see _Future Development_ below.)

## Error Handling

### Construction Errors

`CuspConstructionError` is a `RuntimeException` and is meant as a base class for errors constructing a pipeline. It is a `RuntimeException` because once pipeline construction is implemented, this error should never happen in the future and so it seems redundant to force the user to always provide a catch clause for this class of error.

* `InvalidStageInputException` is thrown if a stage receives an object of a type that it does not expect. (The usage of the `typetools` package as described in the _Implementation Details_ section below should catch this error at code compilation time.)
* `StageAlreadyExistsException` is thrown if the same stage is added to a pipeline twice.
* `StageNotFoundException` is thrown if a route is added to a pipeline that connects to a stage that has not been registered with CUSP.

#### Graph Tests

The use of `JGraphT` to represent the pipeline as a directed graph allows graph theoretic analysis, such as [cycle detection](https://en.wikipedia.org/wiki/Cycle_(graph_theory)#Cycle_detection) and [centrality measures](https://en.wikipedia.org/wiki/Centrality). The former has been implemented, but the latter has not as the need has yet not arisen. The errors in this class derive from `CuspConstructionError` and should throw at compile time, as long as your codebase has a `CuspExecutor#execute` call executed in a test (or if your code calls `Cusp#validateGraph`; finding a way to demote `Cusp#validateGraph` to a private or protected method is a **TODO**; see _Future Development_ below).

Current graph test tests can be run using `Cusp#validateGraph`, which is called in `CuspExecutor#constructPipeline`. Current validation failures are:

* `EmptyPipelineException`: You are trying to execute a pipeline with no stages. Check that you are correctly constructing and executing your pipeline.
* `UnreachableStageException`: You have registered a pipeline stage with CUSP that will never be executed because no execution path beginning with the initial pipeline stage could possibly reach it. Check to make sure that you are registering only the stages that you want to execute and that you have constructed your pipeline so that it can actually execute all of them.
* `InfiniteLoopException`: You have created a loop in your pipeline such that, if your pipeline is executed, it may execute indefinitely. CUSP is not able to detect whether your internal stage logic guarantees that there will not be an infinite loop. CUSP does not allow loops in a pipeline.

The usage of `JGraphT` for these tests means that these are general tests that don't have knowledge of your pipeline stage implementation details. This means that, in contrast to other errors which CUSP can inform, errors in this class will not tell you exactly where the problem is. For example, the infinite loop error will tell you that your pipeline is defined in such a way that a CUSP-illegal possibly infinite loop has been defined, but the error message will not tell you which stages and routes create that loop.

### Execution Errors

`CuspExecutionError` is an `Exception` and is meant as a base class for errors during pipeline execution. It is an `Exception` to force the user of CUSP to handle it somehow.

* `StageFailedException` is thrown if the stage execution throws an exception, and wraps that exception.
* `UnknownExecutionException` should only be thrown if the implementation of CUSP itself has missed a failure case. This should never be thrown.

## Visualizations

### Visualizing the task graph

`CuspExecutor#visualize` can, given a `Cusp` object, create a visualization of the constructed pipeline graph. Note that executing this method call will pop up a GUI and so is meant for development purposes only.

_Note: When IntelliJ is in Presentation mode on MacOS, this will fail for currently uninvestigated and unknown reasons._

### Generating and visualizing execution traces

After executing a pipeline using `CuspExecutor`, calling the executor object's `generateTrace()` method will return a string that looks like:

```
{"planId":104,"planClass":"com.linkedin.parseq.Task$1","traces":[{"id":40000,"name":"queryInventory","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201763444592,"pendingNanos":317201763912695,"endNanos":317201971320161,"taskType":"blocking"},{"id":40001,"name":"async fused","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201763194562,"pendingNanos":317201763418028,"endNanos":317201973958011,"taskType":"fusion"},{"id":40002,"name":"sendEmail","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201762726974,"pendingNanos":317201763060256,"endNanos":317201816557317,"taskType":"blocking"},{"id":40003,"name":"async fused","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201762641760,"pendingNanos":317201762714402,"endNanos":317201821535772,"taskType":"fusion"},{"id":40004,"name":"logRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201762292542,"pendingNanos":317201762621925,"endNanos":317201816557252,"taskType":"blocking"},{"id":40005,"name":"async fused","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201762269574,"pendingNanos":317201762287317,"endNanos":317201819599427,"taskType":"fusion"},{"id":40006,"name":"par","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201762241180,"pendingNanos":317201762265300,"endNanos":317201975535666},{"id":40007,"name":"async fused","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201762191874,"pendingNanos":317201762235324,"endNanos":317201975687127,"taskType":"fusion"},{"id":40008,"name":"queryInventory and sendEmail and logRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201975640844,"pendingNanos":317201975674961,"endNanos":317201975679874},{"id":40009,"name":"failure handler for logRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201819014777,"pendingNanos":317201819105060,"endNanos":317201819122452},{"id":40010,"name":"failure handler for sendEmail","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201820378282,"pendingNanos":317201820498041,"endNanos":317201820515856},{"id":40011,"name":"failure handler for queryInventory","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201973345102,"pendingNanos":317201973476812,"endNanos":317201973489565},{"id":39000,"name":"parseRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201754573362,"pendingNanos":317201755014805,"endNanos":317201756554776,"taskType":"blocking"},{"id":39001,"name":"async fused","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201754456042,"pendingNanos":317201754561615,"endNanos":317201759578040,"taskType":"fusion"},{"id":39002,"name":"failure handler for parseRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201758722518,"pendingNanos":317201758837256,"endNanos":317201758851541},{"id":94,"name":"initialization","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201745056442,"pendingNanos":317201745074776,"endNanos":317201745076947,"taskType":"fusion"},{"id":95,"name":"fused","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201745043020,"pendingNanos":317201745121147,"endNanos":317201745124534,"taskType":"fusion"},{"id":96,"name":"flattened receiveRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201745020091,"pendingNanos":317201745036817,"endNanos":317201751164006,"taskType":"flatten"},{"id":97,"name":"async fused","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201744985456,"pendingNanos":317201745012736,"endNanos":317201753615064,"taskType":"fusion"},{"id":98,"name":"flattened parseRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201744909994,"pendingNanos":317201744974293,"endNanos":317201761601344,"taskType":"flatten"},{"id":99,"name":"async fused","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201744891716,"pendingNanos":317201744906272,"endNanos":317201762133338,"taskType":"fusion"},{"id":100,"name":"flattened queryInventory and sendEmail and logRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201744876569,"pendingNanos":317201744887712,"endNanos":317201975697650,"taskType":"flatten"},{"id":101,"name":"queryInventory recovering with queryBackupSystem","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201744853348,"pendingNanos":317201744872248,"endNanos":317201975740380,"taskType":"withRecover"},{"id":102,"name":"async fused","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201744812746,"pendingNanos":317201744847909,"endNanos":317201975872909,"taskType":"fusion"},{"id":103,"name":"flattened placeOrder","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201744749740,"pendingNanos":317201744802838,"endNanos":317201977415141,"taskType":"flatten"},{"id":41000,"name":"placeOrder","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201975997091,"pendingNanos":317201976200543,"endNanos":317201977010546,"taskType":"blocking"},{"id":41001,"name":"async fused","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201975942983,"pendingNanos":317201975990573,"endNanos":317201977403204,"taskType":"fusion"},{"id":41002,"name":"failure handler for placeOrder","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201977362466,"pendingNanos":317201977391400,"endNanos":317201977393785},{"id":38000,"name":"use output of queryInventory recovering with queryBackupSystem","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201975779983,"pendingNanos":317201975847729,"endNanos":317201975848908},{"id":38001,"name":"recovery","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201975728154,"pendingNanos":317201975734594,"endNanos":317201975735974,"taskType":"recover"},{"id":38002,"name":"use output of flattened parseRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201761825898,"pendingNanos":317201762120151,"endNanos":317201762125188},{"id":38003,"name":"use output of flattened receiveRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201753005891,"pendingNanos":317201753205513,"endNanos":317201753214050},{"id":38004,"name":"use output of initialization","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201745090183,"pendingNanos":317201745116442,"endNanos":317201745116442},{"id":38005,"name":"receiveRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201745183721,"pendingNanos":317201745342315,"endNanos":317201746476752,"taskType":"blocking"},{"id":38006,"name":"async fused","resultType":"SUCCESS","hidden":false,"systemHidden":true,"startNanos":317201745150805,"pendingNanos":317201745177008,"endNanos":317201749916766,"taskType":"fusion"},{"id":38007,"name":"failure handler for receiveRequest","resultType":"SUCCESS","hidden":false,"systemHidden":false,"startNanos":317201749194091,"pendingNanos":317201749287797,"endNanos":317201749306211}],"relationships":[{"relationship":"PARENT_OF","from":101,"to":38001},{"relationship":"PARENT_OF","from":97,"to":38003},{"relationship":"PARENT_OF","from":99,"to":38002},{"relationship":"PARENT_OF","from":95,"to":38004},{"relationship":"SUCCESSOR_OF","from":38004,"to":94},{"relationship":"SUCCESSOR_OF","from":39001,"to":97},{"relationship":"SUCCESSOR_OF","from":38002,"to":98},{"relationship":"SUCCESSOR_OF","from":38006,"to":95},{"relationship":"PARENT_OF","from":39001,"to":39000},{"relationship":"PARENT_OF","from":100,"to":40007},{"relationship":"SUCCESSOR_OF","from":40007,"to":99},{"relationship":"SUCCESSOR_OF","from":38000,"to":101},{"relationship":"PARENT_OF","from":41001,"to":41000},{"relationship":"PARENT_OF","from":40006,"to":40005},{"relationship":"PARENT_OF","from":96,"to":95},{"relationship":"PARENT_OF","from":98,"to":39001},{"relationship":"PARENT_OF","from":39001,"to":39002},{"relationship":"PARENT_OF","from":40006,"to":40001},{"relationship":"PARENT_OF","from":40006,"to":40003},{"relationship":"SUCCESSOR_OF","from":40009,"to":40004},{"relationship":"PARENT_OF","from":103,"to":41001},{"relationship":"SUCCESSOR_OF","from":40011,"to":40000},{"relationship":"PARENT_OF","from":102,"to":101},{"relationship":"PARENT_OF","from":100,"to":99},{"relationship":"PARENT_OF","from":98,"to":97},{"relationship":"PARENT_OF","from":96,"to":38006},{"relationship":"PARENT_OF","from":102,"to":38000},{"relationship":"SUCCESSOR_OF","from":39002,"to":39000},{"relationship":"SUCCESSOR_OF","from":41001,"to":102},{"relationship":"SUCCESSOR_OF","from":41002,"to":41000},{"relationship":"SUCCESSOR_OF","from":38003,"to":96},{"relationship":"SUCCESSOR_OF","from":38001,"to":100},{"relationship":"PARENT_OF","from":97,"to":96},{"relationship":"PARENT_OF","from":38006,"to":38007},{"relationship":"PARENT_OF","from":40001,"to":40011},{"relationship":"PARENT_OF","from":38006,"to":38005},{"relationship":"PARENT_OF","from":40007,"to":40006},{"relationship":"PARENT_OF","from":40003,"to":40010},{"relationship":"PARENT_OF","from":40005,"to":40009},{"relationship":"PARENT_OF","from":95,"to":94},{"relationship":"PARENT_OF","from":40007,"to":40008},{"relationship":"PARENT_OF","from":41001,"to":41002},{"relationship":"PARENT_OF","from":40003,"to":40002},{"relationship":"PARENT_OF","from":40005,"to":40004},{"relationship":"SUCCESSOR_OF","from":40008,"to":40006},{"relationship":"SUCCESSOR_OF","from":40010,"to":40002},{"relationship":"PARENT_OF","from":103,"to":102},{"relationship":"PARENT_OF","from":40001,"to":40000},{"relationship":"PARENT_OF","from":101,"to":100},{"relationship":"SUCCESSOR_OF","from":38007,"to":38005},{"relationship":"PARENT_OF","from":99,"to":98}]}
```

This is generated by [ParSeq](https://github.com/linkedin/parseq), and can be used to generate a visualization of the pipeline output using [`parseq-tracevis-server`](https://github.com/linkedin/parseq/tree/master/subprojects/parseq-tracevis-server), [as per ParSeq documentation](https://github.com/linkedin/parseq/wiki/Tracing). That tool has been vendored in this repository under the `tools` directory, along with a script to start the server.

To generate a visualization:
- make sure graphviz is installed, e.g. `brew install graphviz`
- `cd` to the `tools` directory
- run `./startServer.sh`, go to http://localhost:8080 in your browser
- paste the trace JSON into the textbook.
- enjoy!


## Implementation Details

### Stages

The `Stage` interface is the base type for implementing a discrete unit of execution that either succeeds or fails; it is parameterized by one input and one output type. `AbstractStage` is an abstract class that implements `Stage` and uses the [`typetools`](https://github.com/jhalterman/typetools) package to save some developer boilerplate by automatically resolving generic types for a couple methods, which allows compile time checking that adjacent stages' input and output types match; that is, if you implement your stages by implementing `AbstractStage`, full type checking of stage input/output types will be done automatically at compile time, with descriptive error messages, by Cusp.

### Cusp

Cusp uses a graph representation of the pipeline it is used to construct. In principle, this allows graph theoretic analysis, such as cycle detection or complexity analysis. There has not yet currently been a need.

### CuspExecutor

`CuspExecutor` accepts "compiles" the graph representation held in a `Cusp` object to an executable form using [ParSeq](https://github.com/linkedin/parseq). This design was chosen to allow a pipeline to be executed using a different framework, should be need arise. (For example, one could write an executor for Spark.) In particular, `CuspExecutor` is designed to run on a single machine and not distributed across multiple compute nodes.

## Future Development

### Improve type parameterization of CuspExecutor

See _Executing the pipeline_ above.

### Expose graph centrality measures in an understandable way

Add graph centrality measures that would allow the CUSP user to analyze the nature of their pipeline using graph theoretic methods. As per _Implementation details_ below, subsection _CuspExecutor_, CUSP is designed to allow the implementation of a custom executor that would enable a CUSP pipeline to be executed on a distributed system; at the time of this writing, there has been no need for this type of analysis, but it can be added, and it could be particularly valuable in the case of complex pipelines executed on distributed systems or complex enough pipelines run on a single machine using `CuspExecutor`.

### Inform graph theoretic error detection to be informed by CUSP

See _Graph Tests_ above.
