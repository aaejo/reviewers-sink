package io.github.aaejo.reviewerssink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import io.github.aaejo.reviewerssink.messaging.consumer.ReviewersDataListener;
import io.github.aaejo.messaging.records.Reviewer;

//write a program that takes the reviewer values and adds them to the database
@Service
public class ReviewerDatabaseAddition {


    private final JdbcTemplate jdbcTemplate;

    public ReviewerDatabaseAddition(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void parseValues(Reviewer reviewer) {
        String fName, mName, lName, fullName;
        String salutation;
        String address1 = "";
        String address2 = "";
        String address3 = "";
        String city = "";
        String state = "";
        String postalCode = "";
        String country;
        String department;
        String institution;
        String primeEmail;
        String userID;
        String[] specializations;
        fullName = reviewer.name();
        String[] name = fullName.split(" ");
        if (name.length == 2) {
            fName = name[0];
            lName = name[1];
            mName = "";
        } else if (name.length == 3) {
            fName = name[0];
            mName = name[1];
            lName = name[2];
        } else {
            fName = "";
            mName = "";
            lName = "";
        }

        salutation = reviewer.salutation();
        country = reviewer.institution().country();

        String[] split = reviewer.institution().address().split(",");

        if (country.equals("USA")) {
            if (split.length == 3) {
                address1 = split[0];
                city = split[1];
                for (int i = split[2].length(); i > 0; i--) {
                    if (Character.isLetter(split[2].charAt(i))) {
                        state = split[2].substring(0, i+1);
                        postalCode = split[2].substring(i+2);
                    }
                }
            } else if (split.length == 2) {
                address1 = "";
                city = split[0];
                for (int i = split[1].length(); i > 0; i--) {
                    if (Character.isLetter(split[1].charAt(i))) {
                        state = split[1].substring(0, i+1);
                        postalCode = split[1].substring(i+2);
                    }
                }
            }
            
        } else if (country.equals("United Kingdom")) {
            if (split.length == 4) {
                address1 = split[0] + "," + split[1];
                city = split[2].stripLeading();
                state = "";
                postalCode = split[3].stripLeading();
            } else if (split.length == 3) {
                address1 = split[0];
                city = split[1].stripLeading();
                state = "";
                postalCode = split[2].stripLeading();
            } else if (split.length == 2) {
                address1 = "";
                city = split[0];
                state = "";
                postalCode = split[1].stripLeading();
            }
        } else {
            if (split.length == 4) {
                address1 = split[0];
                city = split[1].stripLeading();
                state = split[2].stripLeading();
                postalCode = split[3].stripLeading();
            } else if (split.length == 3) {
                address1 = "";
                city = split[0];
                state = split[1].stripLeading();
                postalCode = split[2].stripLeading();
            }
        }

        department = reviewer.department();
        institution = reviewer.institution().name();
        primeEmail = reviewer.email();
        userID = reviewer.email();
        specializations = reviewer.specializations();

            for (int i = 0; i < specializations.length; i++) {
                jdbcTemplate.update("INSERT INTO scrapeddata VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", salutation, fName, mName, lName, address1, address2, address3, city, state, postalCode, country, department, institution, primeEmail, userID, specializations[i]);
            }

    }
}
