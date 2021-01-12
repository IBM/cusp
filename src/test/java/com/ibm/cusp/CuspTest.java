/**
 * Copyright (c) 2020 International Business Machines
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ibm.cusp;

import com.ibm.cusp.execution.CuspExecutor;
import com.ibm.cusp.execution.StageOutcomeListener;
import com.ibm.cusp.graph.Cusp;
import com.ibm.cusp.graph.errors.*;
import com.ibm.cusp.graph.observe.CuspObserver;
import com.ibm.cusp.graph.observe.CuspStopwatch;
import com.ibm.cusp.graph.stages.Stage;
import com.ibm.cusp.graph.stages.StageOutcomes;
import com.ibm.cusp.graph.visualize.CuspVisualizer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.internal.matchers.GreaterOrEqual;
import org.mockito.internal.matchers.LessOrEqual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class CuspTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final WidgetRequest request = new WidgetRequest();
    private final Executor taskExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ScheduledExecutorService timerScheduler = Executors.newSingleThreadScheduledExecutor();

    private List<String> sink = Collections.synchronizedList(new ArrayList<>());
    private static final int EXPECTED_LOG_SINK_SIZE = 2;

    private List<String> reportedEvents;

    @Before
    public void sanitize() {
        reportedEvents = Collections.synchronizedList(new ArrayList<>());
        sink = Collections.synchronizedList(new ArrayList<>());
    }

    @Test
    public void it_is_fine_when_everything_is_fine() throws Throwable {
        Cusp cusp = createPipeline(new ParseRequestStage(), new LogRequestStage(sink), new QueryInventoryStage(), new QueryBackupSystem(), new ManufactureWidgetsStage());
        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);

        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);
        Object result = executor.execute();

        logger.debug(executor.generateTrace());

        assertEquals(EXPECTED_LOG_SINK_SIZE, sink.size());
        assertEquals("parsed received WidgetRequest", sink.get(0));
        assertEquals("serialized procured queried parsed received WidgetRequest", result);
    }

    @Test
    public void it_can_report_events_during_stage_execution() throws InterruptedException, UnknownExecutionError, StageFailedException {
        Cusp cusp = createPipeline(new ParseRequestStage(), new LogRequestStage(sink), new QueryInventoryStage(), new QueryBackupSystem(), new ManufactureWidgetsStage());

        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);
        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);
        executor.execute();

        Thread.sleep(1000);
        assertEquals(1, reportedEvents.size());
        assertEquals("parse-request-execution-time was recorded", reportedEvents.get(0));

        logger.debug(executor.generateTrace());
    }

        @Test
    public void it_complains_when_a_stage_is_used_by_two_downstream_stages_directly() throws InterruptedException, UnknownExecutionError, StageFailedException {
        Cusp cusp = new Cusp();
        cusp.addStage(new ParseRequestStage());
        cusp.addStage(new QueryBackupSystem());
        cusp.addStage(new QueryInventoryStage());
        cusp.addStage(new PlaceOrderStage());

        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.QUERY_BACKUP_SYSTEM);
        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.QUERY_INVENTORY);
        cusp.addRoute(WidgetStages.QUERY_BACKUP_SYSTEM, StageOutcomes.SUCCESS, WidgetStages.PLACE_ORDER);
        cusp.addRoute(WidgetStages.QUERY_INVENTORY, StageOutcomes.SUCCESS, WidgetStages.PLACE_ORDER);

        CuspExecutor executor = new CuspExecutor(cusp);
        try {
            executor.constructPipeline(WidgetStages.PARSE_REQUEST, "anything");
            fail();
        } catch(CuspConstructionError expectedException) {
            assertTrue(expectedException.getMessage(), expectedException.getMessage().contains(CuspErrorCode.NONDETERMINISTIC_PIPELINE.toString()));
        }
    }

    @Test
    public void it_complains_when_a_stage_is_used_by_two_downstream_stages_indirectly() throws InterruptedException, UnknownExecutionError, StageFailedException {
        Cusp cusp = new Cusp();
        cusp.addStage(new ParseRequestStage());
        cusp.addStage(new FailingQueryInventoryStage());
        cusp.addStage(new QueryBackupSystem());
        cusp.addStage(new ManufactureWidgetsStage());
        cusp.addStage(new PlaceOrderStage());

        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.MANUFACTURE_WIDGETS);
        cusp.addRoute(WidgetStages.MANUFACTURE_WIDGETS, StageOutcomes.SUCCESS, WidgetStages.PLACE_ORDER);

        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.QUERY_INVENTORY);
        cusp.addRoute(WidgetStages.QUERY_INVENTORY, StageOutcomes.SUCCESS, WidgetStages.PLACE_ORDER);
        cusp.addRoute(WidgetStages.QUERY_INVENTORY, StageOutcomes.RECOVERABLE_FAILURE, WidgetStages.QUERY_BACKUP_SYSTEM);
        cusp.addRoute(WidgetStages.QUERY_BACKUP_SYSTEM, StageOutcomes.SUCCESS, WidgetStages.PLACE_ORDER);

        CuspExecutor executor = new CuspExecutor(cusp);
        try {
            executor.constructPipeline(WidgetStages.PARSE_REQUEST, "anything");
            fail();
        } catch(CuspConstructionError expectedException) {
            assertTrue(expectedException.getMessage(), expectedException.getMessage().contains(CuspErrorCode.NONDETERMINISTIC_PIPELINE.toString()));
        }
    }

    @Test
    public void concurrent_stages_still_execute_when_other_fails_but_recovers() throws Throwable {
        Cusp cusp = createPipeline(new ParseRequestStage(), new LogRequestStage(sink), new FailingQueryInventoryStage(), new QueryBackupSystem(), new ManufactureWidgetsStage());
        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);

        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);
        Object result = executor.execute();

        assertEquals("serialized procured re-queried parsed received WidgetRequest", result);
        assertEquals(EXPECTED_LOG_SINK_SIZE, sink.size());
        assertEquals("parsed received WidgetRequest", sink.get(0));
    }

    @Test
    public void it_recovers_from_failed_stage_that_has_a_recoverable_stage() throws Throwable {
        final String EXPECTED_ERROR_MESSAGE = "STAGE_FAILED: Stage parseRequest failed: failed to parse request";

        Cusp cusp = createPipeline(new FailingParseRequestStage(), new LogRequestStage(sink), new FailingQueryInventoryStage(), new QueryBackupSystem(), new ManufactureWidgetsStage());
        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);
        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);

        try {
            executor.execute();
            fail();
        } catch(StageFailedException e) {
            assertEquals(0, sink.size());
            assertEquals(EXPECTED_ERROR_MESSAGE, e.getMessage());
        }
    }

    @Test
    public void it_recovers_from_failed_stage_when_it_has_a_recoverable_stage() throws Throwable {
        Cusp cusp = createPipeline(new ParseRequestStage(), new LogRequestStage(sink), new FailingQueryInventoryStage(), new QueryBackupSystem(), new ManufactureWidgetsStage());
        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);

        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);
        Object result = executor.execute();

        assertEquals(EXPECTED_LOG_SINK_SIZE, sink.size());
        assertEquals("parsed received WidgetRequest", sink.get(0));
        assertEquals("serialized procured re-queried parsed received WidgetRequest", result);
    }

    @Test
    public void it_recovers_from_failed_reranked_search_and_qpp_when_basic_search_succeeds() throws Throwable {
        Cusp cusp = createPipeline(new ParseRequestStage(), new LogRequestStage(sink), new FailingQueryInventoryStage(), new FailingQueryBackupSystemStage(), new ManufactureWidgetsStage());
        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);

        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);
        Object result = executor.execute();

        assertEquals(EXPECTED_LOG_SINK_SIZE, sink.size());
        assertEquals("parsed received WidgetRequest", sink.get(0));
        assertEquals("serialized procured basicSearch parsed received WidgetRequest", result);
    }

    @Test
    public void it_returns_an_error_when_all_search_stages_fail() throws Throwable {
        final String EXPECTED_ERROR_MESSAGE = "STAGE_FAILED: Stage manufactureWidgets failed: widget manufacture failed!";

        Cusp cusp = createPipeline(new ParseRequestStage(), new LogRequestStage(sink), new FailingQueryInventoryStage(), new FailingQueryBackupSystemStage(), new FailedManufactureWidgetsStage());
        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);

        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);

        try {
            executor.execute();
            fail();
        } catch(StageFailedException expectedException) {
            assertEquals(EXPECTED_LOG_SINK_SIZE, sink.size());
            assertEquals("parsed received WidgetRequest", sink.get(0));
            assertEquals(EXPECTED_ERROR_MESSAGE, expectedException.getMessage());
        }
    }

    @Test
    public void it_reports_success_for_all_stages_with_elapsed_ms() throws Throwable {
        ParseRequestStage parseRequestStage = new ParseRequestStage();
        QueryInventoryStage queryInventoryStage = new QueryInventoryStage();
        Cusp cusp = createPipeline(parseRequestStage, new LogRequestStage(sink), queryInventoryStage, new QueryBackupSystem(), new ManufactureWidgetsStage());
        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);

        StageOutcomeListener mockStageOutcomeListener = mock(StageOutcomeListener.class);
        executor.useStageOutcomeListener(mockStageOutcomeListener);
        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);
        executor.execute();

        verify(mockStageOutcomeListener).success(eq(new WidgetPurchaseRequestStage()), isA(String.class), msGreaterThanOrEqual(0L));
        verify(mockStageOutcomeListener).success(eq(parseRequestStage), isA(String.class), msGreaterThanOrEqual(0L));
        verify(mockStageOutcomeListener).success(eq(new SendEmailStage(sink)), isNull(), msGreaterThanOrEqual(50L));
        verify(mockStageOutcomeListener).success(eq(new LogRequestStage(sink)), isNull(), msGreaterThanOrEqual(50L));
        verify(mockStageOutcomeListener).success(eq(queryInventoryStage), isA(Widgets.class), msGreaterThanOrEqual(200L));
        verify(mockStageOutcomeListener).success(eq(new PlaceOrderStage()), isA(String.class), msGreaterThanOrEqual(0L));
        verifyNoMoreInteractions(mockStageOutcomeListener);
    }

    @Test
    public void it_reports_recovered_stages_with_elapsed_ms() throws Throwable {
        Exception expectedQPPException = new FailingQueryBackupSystemStage.QPPStageException("expected QPP exception");
        Exception expectedRerankedSearchException = new FailingQueryInventoryStage.InventoryQueryException("expected reranked search exception");
        ParseRequestStage parseRequestStage = new ParseRequestStage();
        FailingQueryInventoryStage rerankedSearchStage = new FailingQueryInventoryStage(expectedRerankedSearchException);
        FailingQueryBackupSystemStage searchWithQPPStage = new FailingQueryBackupSystemStage(expectedQPPException);
        ManufactureWidgetsStage manufactureWidgetsStage = new ManufactureWidgetsStage();

        Cusp cusp = createPipeline(parseRequestStage, new LogRequestStage(sink), rerankedSearchStage, searchWithQPPStage, manufactureWidgetsStage);
        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);

        StageOutcomeListener mockStageOutcomeListener = mock(StageOutcomeListener.class);
        executor.useStageOutcomeListener(mockStageOutcomeListener);
        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);
        executor.execute();

        verify(mockStageOutcomeListener).success(eq(new WidgetPurchaseRequestStage()), isA(String.class), msGreaterThanOrEqual(0L));
        verify(mockStageOutcomeListener).success(eq(parseRequestStage), isA(String.class), msGreaterThanOrEqual(0L));
        verify(mockStageOutcomeListener).success(eq(new SendEmailStage(sink)), isNull(), msGreaterThanOrEqual(50L));
        verify(mockStageOutcomeListener).success(eq(new LogRequestStage(sink)), isNull(), msGreaterThanOrEqual(50L));
        verify(mockStageOutcomeListener).recover(eq(rerankedSearchStage), eq(searchWithQPPStage), eq(expectedRerankedSearchException), msGreaterThanOrEqual(0L));
        verify(mockStageOutcomeListener).recover(eq(searchWithQPPStage), eq(manufactureWidgetsStage), eq(expectedQPPException), msGreaterThanOrEqual(0L));
        verify(mockStageOutcomeListener).success(eq(manufactureWidgetsStage), isA(Widgets.class), msGreaterThanOrEqual(0L));
        verify(mockStageOutcomeListener).success(eq(new PlaceOrderStage()), isA(String.class), msGreaterThanOrEqual(0L));
        verifyNoMoreInteractions(mockStageOutcomeListener);
    }

    @Test
    public void it_reports_failed_stages_with_elapsed_ms() throws Throwable {
        Exception expectedQPPException = new FailingQueryBackupSystemStage.QPPStageException("expected QPP exception");
        Exception expectedRerankedSearchException = new FailingQueryInventoryStage.InventoryQueryException("expected reranked search exception");
        Exception expectedBasicSearchException = new FailedManufactureWidgetsStage.WidgetManufactureFailed("expected basic search exception");
        Exception expectedLogPassageException = new FailingLogPassageStage.LogPassageStageException("expected log passage exception");
        ParseRequestStage parseRequestStage = new ParseRequestStage();
        FailingLogPassageStage failingLogPassageStage = new FailingLogPassageStage(expectedLogPassageException);
        FailingQueryInventoryStage rerankedSearchStage = new FailingQueryInventoryStage(expectedRerankedSearchException);
        FailingQueryBackupSystemStage searchWithQPPStage = new FailingQueryBackupSystemStage(expectedQPPException);
        FailedManufactureWidgetsStage basicSearchStage = new FailedManufactureWidgetsStage(expectedBasicSearchException);

        Cusp cusp = createPipeline(parseRequestStage, failingLogPassageStage, rerankedSearchStage, searchWithQPPStage, basicSearchStage);
        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);

        StageOutcomeListener mockStageOutcomeListener = mock(StageOutcomeListener.class);
        executor.useStageOutcomeListener(mockStageOutcomeListener);
        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);
        try {
            executor.execute();
            fail();
        } catch(StageFailedException expectedException) {
            logger.debug("Trace: {}", executor.generateTrace());
            verify(mockStageOutcomeListener).success(eq(new WidgetPurchaseRequestStage()), isA(String.class), msGreaterThanOrEqual(0L));
            verify(mockStageOutcomeListener).success(eq(parseRequestStage), isA(String.class), msGreaterThanOrEqual(0L));
            verify(mockStageOutcomeListener).success(eq(new SendEmailStage(sink)), isNull(), msGreaterThanOrEqual(50L));
            verify(mockStageOutcomeListener).recover(eq(rerankedSearchStage), eq(searchWithQPPStage), eq(expectedRerankedSearchException), msGreaterThanOrEqual(0L));
            verify(mockStageOutcomeListener).recover(eq(searchWithQPPStage), eq(basicSearchStage), eq(expectedQPPException), msGreaterThanOrEqual(0L));
            verify(mockStageOutcomeListener).failure(eq(basicSearchStage), eq(expectedBasicSearchException), msGreaterThanOrEqual(0L));
            verify(mockStageOutcomeListener).failure(eq(failingLogPassageStage), eq(expectedLogPassageException), msGreaterThanOrEqual(0L));
            verifyNoMoreInteractions(mockStageOutcomeListener);
        }
    }

    @Test
    public void it_reports_all_failed_stages_with_elapsed_ms() throws Throwable {
        Exception expectedQueryParseException = new FailingQueryBackupSystemStage.QPPStageException("expected query parse exception");
        Exception expectedQPPException = new FailingQueryBackupSystemStage.QPPStageException("expected QPP exception");
        Exception expectedRerankedSearchException = new FailingQueryInventoryStage.InventoryQueryException("expected reranked search exception");
        Exception expectedBasicSearchException = new FailedManufactureWidgetsStage.WidgetManufactureFailed("expected basic search exception");
        FailingParseRequestStage parseQueryStage = new FailingParseRequestStage(expectedQueryParseException);
        FailingQueryInventoryStage rerankedSearchStage = new FailingQueryInventoryStage(expectedRerankedSearchException);
        FailingQueryBackupSystemStage searchWithQPPStage = new FailingQueryBackupSystemStage(expectedQPPException);
        FailedManufactureWidgetsStage basicSearchStage = new FailedManufactureWidgetsStage(expectedBasicSearchException);

        Cusp cusp = createPipeline(parseQueryStage, new LogRequestStage(sink), rerankedSearchStage, searchWithQPPStage, basicSearchStage);
        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);

        StageOutcomeListener mockStageOutcomeListener = mock(StageOutcomeListener.class);
        executor.useStageOutcomeListener(mockStageOutcomeListener);
        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);
        try {
            executor.execute();
            fail();
        } catch(StageFailedException expectedException) {
            logger.debug("Trace: {}", executor.generateTrace());
            verify(mockStageOutcomeListener).success(eq(new WidgetPurchaseRequestStage()), isA(String.class), msGreaterThanOrEqual(0L));
            verify(mockStageOutcomeListener).recover(eq(rerankedSearchStage), eq(searchWithQPPStage), eq(expectedQueryParseException), eq(0L));
            verify(mockStageOutcomeListener).recover(eq(searchWithQPPStage), eq(basicSearchStage), eq(expectedQueryParseException), eq(0L));
            verify(mockStageOutcomeListener).failure(eq(parseQueryStage), eq(expectedQueryParseException), msLessThanOrEqual(10L));
            verifyNoMoreInteractions(mockStageOutcomeListener);
        }
    }

    @Test
    public void it_reports_successful_stage_outputs() throws Throwable {
        WidgetPurchaseRequestStage widgetPurchaseRequestStage = new WidgetPurchaseRequestStage();
        ParseRequestStage parseRequestStage = new ParseRequestStage();
        Cusp cusp = new Cusp();
        cusp.addStage(widgetPurchaseRequestStage);
        cusp.addStage(parseRequestStage);

        cusp.addRoute(WidgetStages.RECEIVE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.PARSE_REQUEST);
        CuspExecutor executor = new CuspExecutor(cusp, taskExecutor, timerScheduler);

        StageOutcomeListener mockStageOutcomeListener = mock(StageOutcomeListener.class);
        executor.useStageOutcomeListener(mockStageOutcomeListener);
        executor.constructPipeline(WidgetStages.RECEIVE_REQUEST, request);
        executor.execute();


        verify(mockStageOutcomeListener).success(eq(widgetPurchaseRequestStage), eq("received WidgetRequest"), msGreaterThanOrEqual(0L));
        verify(mockStageOutcomeListener).success(eq(parseRequestStage), eq("parsed received WidgetRequest"), msGreaterThanOrEqual(0L));
        verifyNoMoreInteractions(mockStageOutcomeListener);
    }

    @Test
    public void it_complains_when_you_try_to_connect_incompatible_stages() throws StageAlreadyExistsException {
        Cusp cusp = new Cusp();

        cusp.addStage(new WidgetPurchaseRequestStage());
        cusp.addStage(new PlaceOrderStage());

        try {
            cusp.addRoute(WidgetStages.RECEIVE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.PLACE_ORDER);
            fail();
        } catch(AssertionError expectedException) {
            assertTrue(expectedException.getMessage(), expectedException.getMessage().contains(CuspErrorCode.INVALID_STAGE_INPUT_DATA_TYPE.toString()));
        }
    }

    @Test
    public void it_complains_when_you_try_to_create_the_same_stage_twice() {
        Cusp cusp = new Cusp();
        cusp.addStage(new WidgetPurchaseRequestStage());

        try {
            cusp.addStage(new WidgetPurchaseRequestStage());
            fail();
        } catch(StageAlreadyExistsException expectedException) {}
    }

    @Test
    public void it_detects_self_loops() {
        Cusp cusp = new Cusp();
        cusp.addStage(new ParseRequestStage());
        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.PARSE_REQUEST);

        try {
            cusp.validateGraph();
            fail();
        } catch(AssertionError expectedException) {
            assertTrue(expectedException.getMessage(), expectedException.getMessage().contains(CuspErrorCode.INFINITE_LOOP.toString()));
        }
    }

    @Test
    public void it_detects_cycles() {
        Cusp cusp = new Cusp();
        cusp.addStage(new QueryBackupSystem());
        cusp.addStage(new PlaceOrderStage());

        cusp.addRoute(WidgetStages.QUERY_BACKUP_SYSTEM, StageOutcomes.SUCCESS, WidgetStages.PLACE_ORDER);
        cusp.addRoute(WidgetStages.PLACE_ORDER, StageOutcomes.SUCCESS, WidgetStages.QUERY_BACKUP_SYSTEM);

        try {
            cusp.validateGraph();
            fail();
        } catch(AssertionError expectedException) {
            assertTrue(expectedException.getMessage(), expectedException.getMessage().contains(CuspErrorCode.INFINITE_LOOP.toString()));
        }
    }

    @Test
    public void it_detects_unused_stages() {
        Cusp cusp = new Cusp();
        cusp.addStage(new WidgetPurchaseRequestStage());
        cusp.addStage(new ParseRequestStage());
        cusp.addStage(new QueryInventoryStage());

        cusp.addRoute(WidgetStages.RECEIVE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.PARSE_REQUEST);

        try {
            cusp.validateGraph();
            fail();
        } catch(AssertionError expectedException) {
            assertTrue(expectedException.getMessage(), expectedException.getMessage().contains(CuspErrorCode.UNREACHABLE_STAGE.toString()));
        }
    }

    @Test
    public void it_detects_empty_pipeline() {
        Cusp cusp = new Cusp();

        try {
            cusp.validateGraph();
            fail();
        } catch(AssertionError expectedException) {
            assertTrue(expectedException.getMessage(), expectedException.getMessage().contains(CuspErrorCode.EMPTY_PIPELINE.toString()));
        }
    }

    @Test
    public void it_complains_when_you_try_to_use_a_nonexistent_stage() {
        Cusp cusp = new Cusp();
        try {
            cusp.addRoute(WidgetStages.RECEIVE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.PARSE_REQUEST);
            fail();
        } catch(AssertionError expectedException) {
            assertTrue(expectedException.getMessage(), expectedException.getMessage().contains(CuspErrorCode.STAGE_NOT_FOUND.toString()));
        }

    }

    @Test
    @Ignore
    public void it_can_generate_a_visualization_of_the_stage_graph() throws InvalidStageInputException, StageAlreadyExistsException {
        Cusp cusp = createPipeline(new ParseRequestStage(), new LogRequestStage(sink), new FailingQueryInventoryStage(), new QueryBackupSystem(), new ManufactureWidgetsStage());
        CuspVisualizer.visualize(cusp);
    }


    private Cusp createPipeline(Stage parseRequestStage, Stage logRequestStage, Stage queryInventoryStage, Stage queryBackupSystemStage, Stage manufactureWidgetsStage) throws StageAlreadyExistsException, InvalidStageInputException {
        Cusp cusp = new Cusp();

        cusp.addStage(new WidgetPurchaseRequestStage());
        cusp.addStage(parseRequestStage);
        cusp.addStage(new SendEmailStage(sink));
        cusp.addStage(logRequestStage);
        cusp.addStage(queryInventoryStage);
        cusp.addStage(queryBackupSystemStage);
        cusp.addStage(manufactureWidgetsStage);
        cusp.addStage(new PlaceOrderStage());

        cusp.addRoute(WidgetStages.RECEIVE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.PARSE_REQUEST);
        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.SEND_EMAIL);
        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.LOG_REQUEST);
        cusp.addRoute(WidgetStages.PARSE_REQUEST, StageOutcomes.SUCCESS, WidgetStages.QUERY_INVENTORY);
        cusp.addRoute(WidgetStages.QUERY_INVENTORY, StageOutcomes.SUCCESS, WidgetStages.PLACE_ORDER);
        cusp.addRoute(WidgetStages.QUERY_INVENTORY, StageOutcomes.RECOVERABLE_FAILURE, WidgetStages.QUERY_BACKUP_SYSTEM);
        cusp.addRoute(WidgetStages.QUERY_BACKUP_SYSTEM, StageOutcomes.RECOVERABLE_FAILURE, WidgetStages.MANUFACTURE_WIDGETS);

        CuspObserver observer = new CuspObserver() {
            @Override
            public void receiveDuration(CuspStopwatch stopwatch) {
                reportedEvents.add(stopwatch.getIdentifier() + " was recorded");
            }
        };
        cusp.registerObserver(observer);

        return cusp;
    }

    private long msGreaterThanOrEqual(long expected) {
        return longThat(new GreaterOrEqual<>(expected));
    }

    private long msLessThanOrEqual(long expected) {
        return longThat(new LessOrEqual<>(expected));
    }
}
