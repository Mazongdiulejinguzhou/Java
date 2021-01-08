package cn.com.codingce.operation;

import cn.com.codingce.entity.Person;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Streams 支持的操作很丰富，
 * 除了上面介绍的这些比较常用的中间操作，
 * 如 filter或 map（参见Stream Javadoc）外。
 * 还有一些更复杂的操作，如 collect， flatMap以及 reduce。
 *
 * @author 2460798168@qq.com
 * @date 2019/11/11 10:55
 */
public class OperationF {
    public static void main(String[] args) {
        List<Person> persons =
                Arrays.asList(
                        new Person("Max", 18),
                        new Person("Peter", 23),
                        new Person("Pamela", 23),
                        new Person("David", 12));

        /**
         * 高级操作 Collect
         * collect 是一个非常有用的终端操作，
         * 它可以将流中的元素转变成另外一个不同的对象，
         * 例如一个 List， Set或 Map。collect 接受入参为 Collector（收集器），
         * 它由四个不同的操作组成：供应器（supplier）、累加器（accumulator）、组合器（combiner）和终止器（finisher）。
         *
         * 这些都是个啥？别慌，看上去非常复杂的样子，但好在大多数情况下，
         * 您并不需要自己去实现收集器。因为 Java 8通过 Collectors类内置了各种常用的收集器，你直接拿来用就行了。
         *
         * 让我们先从一个非常常见的用例开始：
         */

        List<Person> filtered =
                persons
                        .stream()
                        .filter(p -> p.getName().startsWith("P")) // 过滤出名字以 P 开头的
                        .collect(Collectors.toList()); // 生成一个新的 List

        System.out.println(filtered);    // [Peter, Pamela]

        /**
         * 你也看到了，从流中构造一个 List 异常简单。
         * 如果说你需要构造一个 Set 集合，只需要使用 Collectors.toSet()就可以了。
         *
         * 接下来这个示例，将会按年龄对所有人进行分组：
         */

        Map<Integer, List<Person>> personsByAge = persons
                .stream()
                .collect(Collectors.groupingBy(Person::getAge));  // 以年龄为 key,进行分组  p -> p.getAge();
        personsByAge
                .forEach((age, p) -> System.out.format("age %s: %s\n", age, p));

        /**
         * age 18: [Max]
         * age 23: [Peter, Pamela]
         * age 12: [David]
         *
         * 除了上面这些操作。您还可以在流上执行聚合操作，例如，计算所有人的平均年龄：
         */
        Double averageAge = persons
                .stream()
                .collect(Collectors.averagingInt(Person::getAge)); // 聚合出平均年龄 p -> p.getAge();
        System.out.println(averageAge);     // 19.0

        /**
         * 如果您还想得到一个更全面的统计信息，摘要收集器可以返回一个特殊的内置统计对象。
         * 通过它，我们可以简单地计算出最小年龄、最大年龄、平均年龄、总和以及总数量。
         */
        IntSummaryStatistics ageSummary =
                persons
                        .stream()
                        .collect(Collectors.summarizingInt(Person::getAge)); // 生成摘要统计 p -> p.getAge();
        System.out.println(ageSummary);
        /**
         * IntSummaryStatistics{count=4, sum=76, min=12, average=19.000000, max=23}
         *
         * 下一个示例将所有人连接成一个字符串：
         */
        String phrase = persons
                .stream()
                .filter(p -> p.getAge() >= 18) // 过滤出年龄大于等于18的
                .map(Person::getName) // 提取名字
                .collect(Collectors.joining(" and ", "In Germany ", " are of legal age.")); // 以 In Germany 开头，and 连接各元素，再以 are of legal age. 结束
        System.out.println(phrase);
        /**
         * In Germany Max and Peter and Pamela are of legal age.
         *
         * 联接收集器接受定界符以及可选的前缀和后缀。
         * 为了将流元素转换为映射，我们必须指定如何映射键和值。
         * 请记住，映射的键必须唯一，否则将IllegalStateException抛出。
         * 您可以选择将合并功能作为附加参数传递来绕过异常：
         */
        Map<Integer, String> map = persons
                .stream()
                .collect(Collectors.toMap(
                        Person::getAge,
                        Person::getName,
                        (name1, name2) -> name1 + ";" + name2)); // 对于同样 key 的，将值拼接

        System.out.println(map);
        /**
         * {18=Max, 23=Peter;Pamela, 12=David}
         *
         * 现在我们知道一些最强大的内置收集器，让我们尝试构建自己的特殊收集器。
         * 我们希望将流中的所有人转换为单个字符串，
         * 该字符串包含所有用|竖线字符分隔的大写字母名称。
         * 为了实现这一点，我们通过创建了一个新的收集器Collector.of()。
         * 我们必须通过收集器的四个要素：供应商，累加器，组合器和修整器。
         */
        Collector<Person, StringJoiner, String> personNameCollector =
                Collector.of(
                        () -> new StringJoiner(" | "),          // supplier 供应器
                        (j, p) -> j.add(p.getName().toUpperCase()),     // accumulator 累加器
                        (j1, j2) -> j1.merge(j2),                       // combiner 组合器
                        StringJoiner::toString);                        // finisher 终止器
        String names = persons
                .stream()
                .collect(personNameCollector); // 传入自定义的收集器
        System.out.println(names);              // MAX | PETER | PAMELA | DAVID
        /**
         * 由于Java 中的字符串是 final 类型的，我们需要借助辅助类 StringJoiner，来帮我们构造字符串。
         * 最开始供应器使用分隔符构造了一个 StringJointer。
         * 累加器用于将每个人的人名转大写，然后加到 StringJointer中。
         * 组合器将两个 StringJointer合并为一个。
         * 最终，终结器从 StringJointer构造出预期的字符串。
         */
    }
}
