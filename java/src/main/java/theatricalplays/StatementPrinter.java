package theatricalplays;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class StatementPrinter {

    private record Container(String name, int amount, int audience, int volumeCredits) {
    }

    public String print(Invoice invoice, Map<String, Play> plays) {
        var second = calculateInvoice(invoice, plays);

        return formatInvoice(invoice, second);
    }

    private static String formatInvoice(Invoice invoice, ArrayList<Container> second) {
        var result = String.format("Statement for %s\n", invoice.customer);
        NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);
        for (var item : second) {
            result += String.format("  %s: %s (%s seats)\n", item.name(), frmt.format(item.amount() / 100), item.audience);
        }
        result += String.format("Amount owed is %s\n", frmt.format(second.stream().mapToInt(i -> i.amount()).sum() / 100));
        result += String.format("You earned %s credits\n", second.stream().mapToInt(i -> i.volumeCredits()).sum());
        return result;
    }

    private static ArrayList<Container> calculateInvoice(Invoice invoice, Map<String, Play> plays) {
        var second = new ArrayList<Container>();
        for (var perf : invoice.performances) {
            var play = plays.get(perf.playID);
            var thisAmount = 0;
            var volumeCredits = 0;

            switch (play.type) {
                case "tragedy":
                    thisAmount = 40000;
                    if (perf.audience > 30) {
                        thisAmount += 1000 * (perf.audience - 30);
                    }
                    break;
                case "comedy":
                    thisAmount = 30000;
                    if (perf.audience > 20) {
                        thisAmount += 10000 + 500 * (perf.audience - 20);
                    }
                    thisAmount += 300 * perf.audience;
                    break;
                default:
                    throw new Error("unknown type: ${play.type}");
            }

            // add volume credits
            volumeCredits += Math.max(perf.audience - 30, 0);
            // add extra credit for every ten comedy attendees
            if ("comedy".equals(play.type)) volumeCredits += Math.floor(perf.audience / 5);

            second.add(new Container(play.name, thisAmount, perf.audience, volumeCredits));
        }
        return second;
    }

}
