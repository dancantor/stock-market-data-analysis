package fd;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class MarketImpactProcessor {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> sourceNewsImpact = env.socketTextStream("socket-market-impact", 9999);

        DataStream<Tuple2<String, Double>> newsStream = sourceNewsImpact.map(new MapFunction<String, Tuple2<String, Double>>() {
            @Override
            public Tuple2<String, Double> map(String value) {
                String[] parts = value.split(",");
                return new Tuple2<String, Double>(parts[0], Double.parseDouble(parts[1]));
            }
        });

        DataStream<String> alertStream = newsStream
                .keyBy(event -> event.f0)
                .process(new TrackCumulativeImpactWithAlerts());

        alertStream.print();
        env.execute("Market Impact Processor");
    }

    public static class TrackCumulativeImpactWithAlerts extends KeyedProcessFunction<String, Tuple2<String, Double>, String> {
        private transient ValueState<Double> cumulativeImpactState;

        @Override
        public void open(OpenContext openContext) throws Exception {
            ValueStateDescriptor<Double> descriptor = new ValueStateDescriptor<>("cumulativeImpact", Double.class);
            cumulativeImpactState = getRuntimeContext().getState(descriptor);
        }

        @Override
        public void processElement(Tuple2<String, Double> newsImpactEvent, KeyedProcessFunction<String, Tuple2<String, Double>, String>.Context context, Collector<String> collector) throws Exception {
            Double cumulativeImpact = cumulativeImpactState.value();

            if (cumulativeImpact == null) {
                cumulativeImpact = 0.0;
            }

            cumulativeImpact += newsImpactEvent.f1;
            cumulativeImpactState.update(cumulativeImpact);

            if (Math.abs(cumulativeImpact) >= 3.0) {
                collector.collect("ALERT: Cumulative impact on " + newsImpactEvent.f0 + " exceeded threshold(3.0): " + cumulativeImpact);
                cumulativeImpactState.clear();
            }
        }
    }
}
