package hrc.komuni.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {

    public static String formatDate(String inputDate) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        Date date = inputFormat.parse(inputDate);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = outputFormat.format(date);
        System.out.println(formattedDate);
        return formattedDate;
    }
}


