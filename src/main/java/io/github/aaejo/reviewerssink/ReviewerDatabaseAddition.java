package io.github.aaejo.reviewerssink;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import io.github.aaejo.messaging.records.Reviewer;
import io.github.aaejo.reviewerssink.address.Address;
import io.github.aaejo.reviewerssink.address.AddressParser;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeffery Kung
 * @author Omri Harary
 */
@Slf4j
@Service
public class ReviewerDatabaseAddition {
    private final JdbcTemplate jdbcTemplate;
    private final AddressParser addressParser;

    public ReviewerDatabaseAddition(JdbcTemplate jdbcTemplate, AddressParser addressParser) {
        this.jdbcTemplate = jdbcTemplate;
        this.addressParser = addressParser;
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

        if (StringUtils.startsWith(reviewer.name(), "Dr ")) {
            fullName = StringUtils.removeStart(reviewer.name(), "Dr ");
        } else if (StringUtils.startsWith(reviewer.name(), "Dr. ")) {
            fullName = StringUtils.removeStart(reviewer.name(), "Dr. ");
        } else if (StringUtils.startsWith(reviewer.name(), "Professor ")) {
            fullName = StringUtils.removeStart(reviewer.name(), "Professor ");
        } else {
            fullName = reviewer.name();
        }
        String[] name = fullName.split(" ");
        if (name.length == 2) {
            fName = name[0];
            lName = name[1];
            mName = "";
        } else if (name.length >= 3) {
            fName = name[0];
            lName = name[name.length - 1];
            mName = StringUtils.trimToEmpty(StringUtils.join(Arrays.copyOfRange(name, 1, name.length - 1), " "));
        } else {
            fName = "";
            mName = "";
            lName = "";
        }

        salutation = reviewer.salutation();
        country = reviewer.institution().country();

        Address address = addressParser.parse(reviewer.institution().address());

        address1 = address.getDbFormatAddress();
        city = address.city();
        state = address.state();
        postalCode = address.postcode();

        department = reviewer.department();
        institution = reviewer.institution().name();
        primeEmail = reviewer.email();
        userID = reviewer.email();
        specializations = reviewer.specializations();

        for (int i = 0; i < specializations.length; i++) {
            try {
                jdbcTemplate.update("INSERT INTO scrapeddata VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                        salutation, fName, mName, lName, address1, address2, address3, city, state, postalCode, country,
                        department, institution, primeEmail, userID, specializations[i]);
            } catch (DataAccessException e) {
                // TODO: Improve this
                log.error("DB insertion error.", e);
            }
        }
    }
}
