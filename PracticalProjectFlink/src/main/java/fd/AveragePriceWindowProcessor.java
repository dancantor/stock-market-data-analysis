package fd;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.evictors.CountEvictor;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;

public class AveragePriceWindowProcessor {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> sourceStockPrice = env.socketTextStream("socket-average-price", 9998);

        DataStream<Tuple2<String, Double>> splitStockPrice = sourceStockPrice.map(new MapFunction<String, Tuple2<String, Double>>() {
            @Override
            public Tuple2<String, Double> map(String value){
                String[] words = value.split(",");
                return new Tuple2<>(words[0], Double.parseDouble(words[1]));
            }
        });

        splitStockPrice
            .keyBy(event -> event.f0)
            .window(GlobalWindows.create())
            .trigger(CountTrigger.of(4))
            .evictor(CountEvictor.of(2))
            .apply(new RichWindowFunction<Tuple2<String, Double>, Tuple2<String, Double>, String, GlobalWindow>() {
                @Override
                public void apply(String s, GlobalWindow globalWindow, Iterable<Tuple2<String, Double>> windowedEvents, Collector<Tuple2<String, Double>> collector) throws Exception {
                    double sum = 0;
                    double count = 0;
                    for (Tuple2<String, Double> event : windowedEvents) {
                        sum += event.f1;
                        count += 1;
                    }
                    double average = sum / count;
                    collector.collect(new Tuple2<>(s, average));
                }
            })
            .map(new MapFunction<Tuple2<String, Double>, String>() {
                @Override
                public String map(Tuple2<String, Double> value){

                    return "Average Price: " + value.f0 + "->" + value.f1;
                }
            })
            .print();

        env.execute("Trade Volume Processor");

    }
}
