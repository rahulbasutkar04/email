package org.example.email;

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Main {

    // Data extract and function call for mail
    static String toMail;
    static String subject;
    static String body;

    static String filePath = "src/files/birthdays.txt";

    public static String [] upComingBirthday() {
        String [] data=new String[3];
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true; // Flag to skip the header line

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date currentDate = new Date(); // Get current date

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(currentDate);

            // Get tomorrow's date
            Calendar tomorrowCalendar = Calendar.getInstance();
            tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);

            // Map to store information of individuals whose birthday is tomorrow
            Map<String, String> tomorrowBirthdayMap = new HashMap<>();

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip the header line
                    continue;
                }
                String[] parts = line.trim().split(", ");
                if (parts.length == 4) {
                    String name = parts[1] + " " + parts[0]; // Concatenate first name and last name
                    String dobString = parts[2];

                    Date dobDate = dateFormat.parse(dobString);

                    Calendar dobCalendar = Calendar.getInstance();
                    dobCalendar.setTime(dobDate);

                    // Set the year of the birthday to the current year
                    dobCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR));

                    // Check if the birthday is tomorrow
                    if (dobCalendar.get(Calendar.MONTH) == tomorrowCalendar.get(Calendar.MONTH)
                            && dobCalendar.get(Calendar.DAY_OF_MONTH) == tomorrowCalendar.get(Calendar.DAY_OF_MONTH)) {
                        tomorrowBirthdayMap.put(name, line);
                        System.out.println("Tomorrow is " + name + "'s birthday!");
                        System.out.println("Name: " + name);
                        System.out.println("DOB: " + dobString);
                        System.out.println("Email: " + parts[3]);
                        System.out.println(); // Print an empty line for readability

                        // Set values for email

                        toMail = parts[3];
                        subject = "Happy birthday!";
                        body = "Happy birthday, dear " + name + "!\n\nBest wishes on your special day!";

                        data[0]=toMail;
                        data[1]=subject;
                        data[2]=body;
               return  data;

                    }
                } else {
                    System.out.println("Invalid data format: " + line);
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return  data;
    }

    @Autowired
    private EmailSenderService senderService;

    public static void main(String[] args) {
         // Call method to set values for email
        SpringApplication.run(Main.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void triggerMail() throws MessagingException {
        String[] birthdayData = upComingBirthday();
        if (birthdayData != null && birthdayData[0] != null) {
            String toMail = birthdayData[0];
            String subject = birthdayData[1];
            String body = birthdayData[2];

            if (senderService != null) {
                senderService.sendSimpleEmail(toMail, subject, body);
            } else {
                System.out.println("Email sender service is not initialized.");
            }
        } else {
            System.out.println("No upcoming birthdays found..");
        }
    }


}

