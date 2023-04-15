package io.github.aaejo.reviewerssink.address;

import java.util.List;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Convenient Java-ish version of libpostal {@code libpostal_address_parser_response_t}.
 * Fields documented in https://github.com/openvenues/libpostal/tree/master#parser-labels
 * 
 * @author Omri Harary
 */
public record Address(String house,
        String category,
        String near,
        String houseNumber,
        String road,
        String unit,
        String level,
        String staircase,
        String entrance,
        String poBox,
        String postcode,
        String suburb,
        String cityDistrict,
        String city,
        String island,
        String stateDistrict,
        String state,
        String countryRegion,
        String country,
        String worldRegion) {

    public String getDbFormatAddress() {
        List<String> components = List.of(house, houseNumber, road, unit, cityDistrict);
        String dbFormatAddress = StringUtils.join(components, " ");
        dbFormatAddress = StringUtils.trim(dbFormatAddress);
        dbFormatAddress = RegExUtils.replaceAll(dbFormatAddress, "\\s+", " ");
        return dbFormatAddress;
    }
}
