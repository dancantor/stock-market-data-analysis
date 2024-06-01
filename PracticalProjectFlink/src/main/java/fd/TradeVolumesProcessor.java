package fd;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class TradeVolumesProcessor {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> sourceTradeVolumes = env.socketTextStream("socket-trade-volumes", 9997);

        DataStream<Tuple2<String, Integer>> splitTradeVolumes = sourceTradeVolumes.map(new MapFunction<String, Tuple2<String, Integer>>() {

            @Override
            public Tuple2<String, Integer> map(String value){
                String[] words = value.split(",");
                return new Tuple2<>(words[0], Integer.parseInt(words[1]));
            }
        });

        splitTradeVolumes
                .keyBy(event -> event.f0)
                .sum(1)
                .map(new MapFunction<Tuple2<String, Integer>, String>() {
                    @Override
                    public String map(Tuple2<String, Integer> value){

                        return "Total Trade Volume: " + value.f0 + "->" + value.f1;
                    }
                })
                .print();
        env.execute("Trade Volume Processor");

    }
}