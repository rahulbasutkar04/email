package org.example.email;

import org.example.email.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
public class Main {

    static String filePath = "src/files/birthdays.txt";

    @Autowired
    private EmailSenderService senderService;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)

    public void triggerMail() throws MessagingException {
        Map<Date, List<String>> upcomingBirthdays = findUpcomingBirthdays();

        if (upcomingBirthdays.isEmpty()) {
            System.out.println("No upcoming birthdays found.");
            return;
        }

        for (Map.Entry<Date, List<String>> entry : upcomingBirthdays.entrySet()) {
            Date birthdayDate = entry.getKey();
            List<String> birthdayDetails = entry.getValue();

            for (String birthdayDetail : birthdayDetails) {
                String[] birthdayData = birthdayDetail.split(", ");
                String name = birthdayData[1] + " " + birthdayData[0];
                String dob = birthdayData[2];
                String toMail = birthdayData[3];

                // Print birthday information
                System.out.println("Birthday Person: " + name);
                System.out.println("DOB: " + dob);
                System.out.println("Sending email...");

                // Send email
                String subject = "Happy Birthday!";
                String body = "Happy birthday, dear " + name + "!\n\nBest wishes on your special day!";
                senderService.sendSimpleEmail(toMail, subject, body);

                System.out.println("Email sent to " + toMail);
                System.out.println();
            }
        }
    }


    public static Map<Date, List<String>> findUpcomingBirthdays() {
        Map<Date, List<String>> upcomingBirthdays = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true; // Flag to skip the header line

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar currentCalendar = Calendar.getInstance();
            Calendar tomorrowCalendar = Calendar.getInstance();
            tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] parts = line.trim().split(", ");
                if (parts.length == 4) {
                    Date dobDate = dateFormat.parse(parts[2]);

                    Calendar dobCalendar = Calendar.getInstance();
                    dobCalendar.setTime(dobDate);

                    // Set the year of the birthday to the current year
                    dobCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR));

                    // Check if the birthday is within the next 24 hours
                    if (dobCalendar.get(Calendar.MONTH) == tomorrowCalendar.get(Calendar.MONTH)
                            && dobCalendar.get(Calendar.DAY_OF_MONTH) == tomorrowCalendar.get(Calendar.DAY_OF_MONTH)) {
                        // Add the individual to the map
                        upcomingBirthdays.computeIfAbsent(dobDate, k -> new ArrayList<>()).add(line);
                    }
                } else {
                    System.out.println("Invalid data format: " + line);
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return upcomingBirthdays;
    }

}
