package com.studiomediatech.queryresponse;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.junit.RabbitAvailable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@RabbitAvailable
class QueryResponseConfigurationTest {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(QueryResponseConfiguration.class, RabbitAutoConfiguration.class));

    @Test
    void ensureProvidesQueryBuilderBean() throws Exception {

        contextRunner.run(ctx -> {
            assertThat(ctx.getBean(QueryBuilder.class)).isNotNull();
            assertThat(ctx.getBean("queryBuilder")).isNotNull();
        });
    }

    @Test
    void ensureProvidesResponseBuilderBean() throws Exception {

        contextRunner.run(ctx -> {
            assertThat(ctx.getBean(ResponseBuilder.class)).isNotNull();
            assertThat(ctx.getBean("responseBuilder")).isNotNull();
        });
    }

    @Test
    void ensureConfiguresRabbitFacade() throws Exception {

        contextRunner.run(ctx -> assertThat(ctx.getBean(RabbitFacade.class)).isNotNull());
    }

    @Test
    @DisplayName("query registry bean is created")
    void ensureConfiguresQueryRegistry() {

        contextRunner.run(ctx -> assertThat(ctx.getBean(QueryRegistry.class)).isNotNull());
    }

    @Test
    @DisplayName("query registry bean is provided as instance")
    void ensureConfiguresQueryRegistryAndSelfInjects() {

        contextRunner.run(ctx -> {
            QueryRegistry bean = ctx.getBean(QueryRegistry.class);

            assertThat(bean).isNotNull();
            assertThat(QueryRegistry.instance.get()).isEqualTo(bean);
        });
    }

    @Test
    @DisplayName("response registry bean is created")
    void ensureConfiguresResponseRegistry() {

        contextRunner.run(ctx -> assertThat(ctx.getBean(ResponseRegistry.class)).isNotNull());
    }

    @Test
    @DisplayName("response registry bean is provided as instance")
    void ensureConfiguresResponseRegistryAndSelfInjects() {

        contextRunner.run(ctx -> {
            ResponseRegistry bean = ctx.getBean(ResponseRegistry.class);

            assertThat(bean).isNotNull();
            assertThat(ResponseRegistry.instance.get()).isEqualTo(bean);
        });
    }

    @Test
    @DisplayName("a statistics component bean is wired by the configuration")
    void ensureWiresStatisticsComponent() throws Exception {

        contextRunner.run(ctx -> assertThat(ctx.getBean(Statistics.class)).isNotNull());
    }

    @Test
    void ensureQueryResponseTopicExchangeBeanIsNonDurableAndAutoDelete() throws Exception {

        QueryResponseConfiguration sut = new QueryResponseConfiguration(new QueryResponseConfigurationProperties());

        TopicExchange exchange = sut.queryResponseTopicExchange();

        assertThat(exchange.getName()).isEqualTo("query-response");
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }

    @Test
    void ensureQueryResponseFanoutExchangeIsDurableAndNotAutoDelete() throws Exception {

        QueryResponseConfiguration sut = new QueryResponseConfiguration(new QueryResponseConfigurationProperties());

        FanoutExchange exchange = sut.queryResponseFanoutExchange();

        assertThat(exchange.getName()).isEqualTo("query-response.fanout");
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }
}
