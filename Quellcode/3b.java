public class MysteriousDataProcessor {
  public static class MyMapper extends Mapper<Object, Text, Text, Text> {
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String input = value.toString().toLowerCase();
      for (int index = 0;
      index < input.length() - 1;
      index++) {
        Text substr = new Text(input.
        substring(index, index + 2));
        context.write(value, substr);
      }
    }
  }
  public static class MyReducer extends Reducer<Text, Text, Text, IntWritable> {
    public void reduce(Text key, Iterable<Text> values, Context context)
      throws IOException, InterruptedException {
      HashSet<String> set = new HashSet<String>();
      for (Text substr : values) {
        set.add(substr.toString());
      }
      context.write(key, new IntWritable(set.size()));
    }
  }
}