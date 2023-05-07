package io.github.aaejo.reviewerssink;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.tupilabs.human_name_parser.HumanNameParserBuilder;
import com.tupilabs.human_name_parser.HumanNameParserParser;

import io.github.aaejo.messaging.records.Reviewer;
import io.github.aaejo.reviewerssink.address.Address;
import io.github.aaejo.reviewerssink.address.AddressParser;

/**
 * @author Jeffery Kung
 * @author Omri Harary
 */
@Service
public class ReviewerDatabaseAddition {
    private static final Logger log = LoggerFactory.getLogger(ReviewerDatabaseAddition.class);

    private final JdbcTemplate jdbcTemplate;
    private final AddressParser addressParser;

    public ReviewerDatabaseAddition(JdbcTemplate jdbcTemplate, AddressParser addressParser) {
        this.jdbcTemplate = jdbcTemplate;
        this.addressParser = addressParser;
    }

    public void parseValues(Reviewer reviewer) {
        String fName, mName, lName;
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

        HumanNameParserParser nameParser = new HumanNameParserBuilder(reviewer.name())
                .withExtraSalutations(List.of("professor"))
                .build();

        if (StringUtils.isNotBlank(nameParser.getSalutation())) {
            salutation = nameParser.getSalutation();
        } else {
            salutation = reviewer.salutation();
        }

        if (StringUtils.isNotBlank(nameParser.getLeadingInit())) {
            fName = nameParser.getLeadingInit() + " " + nameParser.getFirst();
        } else {
            fName = nameParser.getFirst();
        }

        if (StringUtils.isNotBlank(nameParser.getSuffix()) && StringUtils.isNotBlank(nameParser.getPostnominal())) {
            lName = nameParser.getLast() + " " + nameParser.getSuffix() + " " + nameParser.getPostnominal();
        } else if (StringUtils.isNotBlank(nameParser.getSuffix())) {
            lName = nameParser.getLast() + " " + nameParser.getSuffix();
        } else if (StringUtils.isNotBlank(nameParser.getPostnominal())) {
            lName = nameParser.getLast() + " " + nameParser.getPostnominal();
        } else {
            lName = nameParser.getLast();
        }

        if (StringUtils.isNotBlank(nameParser.getNicknames())) {
            mName = nameParser.getNicknames() + " " + nameParser.getMiddle();
        } else {
            mName = nameParser.getMiddle();
        }

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
