package com.studiomediatech.queryresponse;

import com.studiomediatech.queryresponse.Statistics.Stat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationContext;

import org.springframework.mock.env.MockEnvironment;

import org.springframework.scheduling.TaskScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class StatisticsTest {

    @Mock
    ApplicationContext ctx;

    @Mock
    ResponseRegistry registry;

    @Mock
    RabbitFacade rabbitFacade;
    @Mock
    TaskScheduler taskScheduler;

    @Captor
    ArgumentCaptor<ChainingResponseBuilder<?>> responses;

    @SuppressWarnings("static-access")
    @Test
    void ensureBuildsResponseOnApplicationReady() throws InterruptedException {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();

        new Statistics(env, ctx, rabbitFacade, taskScheduler).respond();

        verify(registry).register(responses.capture());

        ChainingResponseBuilder<?> builder = responses.getValue();
        assertThat(builder).isNotNull();

        assertThat(builder.getRespondToTerm()).isEqualTo("query-response/stats");
        assertThat(builder.elements().get()).isNotNull();
        assertThat(builder.elements().get().hasNext()).isTrue();

        List<Stat> stats = new ArrayList<>();
        builder.elements().get().forEachRemaining(obj -> stats.add((Stat) obj));
        assertThat(stats.stream().map(s -> s.key).collect(Collectors.toList())).contains("count_queries");

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensureMetaWithPublishingOnlyStatus() throws Exception {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);

        String key = "only_responses";
        Stat s1 = sut.getStats().stream().filter(s -> key.equals(s.key)).findFirst().get();

        assertThat(s1.value).isEqualTo(true);
        assertThat(s1.uuid).isEqualTo(sut.uuid);

        sut.onlyResponses.set(false);

        Stat s2 = sut.getStats().stream().filter(s -> key.equals(s.key)).findFirst().get();
        assertThat(s2.value).isEqualTo(false);

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensureMetaWithPidPartOfStats() throws Exception {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);
        sut.pidSupplier = () -> "some-pid";

        String key = "pid";
        Stat stat = sut.getStats().stream().filter(s -> key.equals(s.key)).findFirst().get();

        assertThat(stat.value).isEqualTo("some-pid");
        assertThat(stat.uuid).isEqualTo(sut.uuid);

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensureMetaWithNamePartOfStats() throws Exception {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);
        sut.nameSupplier = () -> "some-name";

        String key = "name";
        Stat stat = sut.getStats().stream().filter(s -> key.equals(s.key)).findFirst().get();

        assertThat(stat.value).isEqualTo("some-name");
        assertThat(stat.uuid).isNotEmpty();

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensureMetaWithHostPartOfStats() throws Exception {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);
        sut.hostSupplier = () -> "some-host";

        String key = "host";
        Stat stat = sut.getStats().stream().filter(s -> key.equals(s.key)).findFirst().get();

        assertThat(stat.value).isEqualTo("some-host");
        assertThat(stat.uuid).isNotEmpty();

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensureMetaWithUptimePartOfStats() throws Exception {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);
        sut.uptimeSupplier = () -> "some-uptime";

        String key = "uptime";
        Stat stat = sut.getStats().stream().filter(s -> key.equals(s.key)).findFirst().get();

        assertThat(stat.value).isEqualTo("some-uptime");
        assertThat(stat.uuid).isNotEmpty();

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensureQueriesCountPartOfStats() throws Exception {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);

        String stat = "count_queries";

        assertThat(sut.getStats().stream().filter(s -> stat.equals(s.key)).map(s -> (long) s.value)
            .findFirst().get()).isEqualTo(0L);

        sut.incrementPublishedQueriesCounter();
        sut.incrementPublishedQueriesCounter();
        sut.incrementPublishedQueriesCounter();

        Stat s = sut.getStats().stream().filter(obj -> stat.equals(obj.key)).findFirst().get();
        assertThat((long) s.value).isEqualTo(3L);
        assertThat(s.uuid).isEqualTo(sut.uuid);

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensureConsumedResponsesCountPartOfStats() throws Exception {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);

        String stat = "count_consumed_responses";

        assertThat(sut.getStats().stream().filter(s -> stat.equals(s.key)).map(s -> (long) s.value)
            .findFirst().get()).isEqualTo(0L);

        sut.incrementConsumedResponsesCounter();
        sut.incrementConsumedResponsesCounter();
        sut.incrementConsumedResponsesCounter();

        Stat s = sut.getStats().stream().filter(obj -> stat.equals(obj.key)).findFirst().get();
        assertThat((long) s.value).isEqualTo(3L);
        assertThat(s.uuid).isEqualTo(sut.uuid);

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensurePublishedResponsesCountPartOfStats() throws Exception {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);

        String stat = "count_published_responses";

        assertThat(sut.getStats().stream().filter(s -> stat.equals(s.key)).map(s -> (long) s.value)
            .findFirst().get()).isEqualTo(0L);

        sut.incrementPublishedResponsesCounter();
        sut.incrementPublishedResponsesCounter();
        sut.incrementPublishedResponsesCounter();

        Stat s = sut.getStats().stream().filter(obj -> stat.equals(obj.key)).findFirst().get();
        assertThat((long) s.value).isEqualTo(3L);
        assertThat(s.uuid).isEqualTo(sut.uuid);

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensureQueriesThroughputPartOfStats() throws Exception {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);

        String stat = "throughput_queries";

        assertThat(sut.getStats().stream().filter(s -> stat.equals(s.key)).map(s -> (long) s.value)
            .findFirst().get()).isEqualTo(0L);

        sut.incrementPublishedQueriesCounter();
        sut.incrementPublishedQueriesCounter();
        sut.incrementPublishedQueriesCounter();

        Stat s = sut.getStats().stream().filter(obj -> stat.equals(obj.key)).findFirst().get();
        assertThat((long) s.value).isEqualTo(3L);
        assertThat(s.uuid).isEqualTo(sut.uuid);
        assertThat(s.timestamp).isNotNull();

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensureResponsesThroughputPartOfStats() throws Exception {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);

        String stat = "throughput_responses";

        assertThat(sut.getStats().stream().filter(s -> stat.equals(s.key)).map(s -> (long) s.value)
            .findFirst().get()).isEqualTo(0L);

        sut.incrementPublishedResponsesCounter();
        sut.incrementPublishedResponsesCounter();
        sut.incrementPublishedResponsesCounter();
        sut.incrementPublishedResponsesCounter();

        Stat s = sut.getStats().stream().filter(obj -> stat.equals(obj.key)).findFirst().get();
        assertThat((long) s.value).isEqualTo(4L);
        assertThat(s.uuid).isEqualTo(sut.uuid);
        assertThat(s.timestamp).isNotNull();

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensureFallbacksCountPartOfStats() throws Exception {

        ResponseRegistry.instance = () -> registry;

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);

        String stat = "count_fallbacks";

        assertThat(sut.getStats().stream().filter(s -> stat.equals(s.key)).map(s -> (long) s.value)
            .findFirst().get()).isEqualTo(0L);

        sut.incrementFallbacksCounter();
        sut.incrementFallbacksCounter();
        sut.incrementFallbacksCounter();

        Stat s = sut.getStats().stream().filter(obj -> stat.equals(obj.key)).findFirst().get();
        assertThat((long) s.value).isEqualTo(3L);
        assertThat(s.uuid).isEqualTo(sut.uuid);

        ResponseRegistry.instance = () -> null;
    }


    @Test
    void ensureMeasuresAndRetainsLatencyStatisticsInBoundedCollection() throws Exception {

        MockEnvironment env = new MockEnvironment();
        Statistics sut = new Statistics(env, ctx, rabbitFacade, taskScheduler);

        // NOOP
        sut.measureLatency(null, 43L);

        assertThat(sut.getMinLatency()).isNull();
        assertThat(sut.getMaxLatency()).isNull();
        assertThat(sut.getAvgLatency()).isEqualTo(0.0, within(1.0));

        for (long i = 1; i < 1000; i++) {
            // NOOP
            sut.measureLatency(1L, 1L);
        }

        assertThat(sut.getMinLatency()).isNull();
        assertThat(sut.getMaxLatency()).isNull();
        assertThat(sut.getAvgLatency()).isEqualTo(0.0, within(1.0));

        // Given that we have measured 1000 20ms latencies
        for (long i = 1; i < 1000; i++) {
            sut.measureLatency(i, i + 20);
        }

        assertThat(sut.getMinLatency()).isEqualTo(20);
        assertThat(sut.getMaxLatency()).isEqualTo(20);
        assertThat(sut.getAvgLatency()).isEqualTo(20.0, within(1.0));

        // If we measure one 100ms latencies
        sut.measureLatency(1L, 101L);

        // Retains previous min/avg
        assertThat(sut.getMinLatency()).isEqualTo(20);
        assertThat(sut.getMaxLatency()).isEqualTo(100);
        assertThat(sut.getAvgLatency()).isEqualTo(20.0, within(1.0));

        // If we measure 1000 100ms latencies
        for (long i = 1; i < 1000; i++) {
            sut.measureLatency(i, i + 100);
        }

        // Filled
        assertThat(sut.getMinLatency()).isEqualTo(100);
        assertThat(sut.getMaxLatency()).isEqualTo(100);
        assertThat(sut.getAvgLatency()).isEqualTo(100.0, within(1.0));
    }


    @Test
    void ensureCanHaveStatWithOrWithoutTimestampAndUUID() throws Exception {

        Stat s1 = Statistics.Stat.of("foo", 123);
        Stat s2 = Statistics.Stat.at("bar", 42, "some-uuid");

        assertThat(s1.timestamp).isNull();
        assertThat(s2.timestamp).isNotNull();
    }


    @Test
    void ensurePrettyToStringForStats() throws Exception {

        Stat s1 = Statistics.Stat.of("foo", 123);
        Stat s2 = Statistics.Stat.at("bar", 42, "some-uuid");
        Stat s3 = Statistics.Stat.from("bar", 42, "some-uuid");

        assertThat(s1.toString()).isEqualTo("foo=123");
        assertThat(s2.toString()).startsWith("bar=42 at=");
        assertThat(s2.toString()).endsWith(" from=some-uuid");
        assertThat(s3.toString()).startsWith("bar=42 from=some-uuid");
    }
}
