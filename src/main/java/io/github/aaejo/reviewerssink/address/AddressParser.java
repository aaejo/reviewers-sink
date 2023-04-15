package io.github.aaejo.reviewerssink.address;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.SizeTPointer;
import org.bytedeco.libpostal.libpostal_address_parser_options_t;
import org.bytedeco.libpostal.libpostal_address_parser_response_t;
import org.bytedeco.libpostal.libpostal_data;
import org.bytedeco.libpostal.libpostal_normalize_options_t;
import org.bytedeco.libpostal.global.postal;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Address parser service wrapping libpostal.
 * https://github.com/openvenues/libpostal
 * https://github.com/bytedeco/javacpp-presets/tree/master/libpostal
 * 
 * @author Omri Harary 
 */
@Slf4j
@Service
public class AddressParser {

    private final libpostal_address_parser_options_t addressParserOptions;
    private final libpostal_normalize_options_t normalizeOptions;

    public AddressParser() throws IOException, InterruptedException {
        Path dataDir = Files.createTempDirectory("libpostal-data");
        String libpostalData = Loader.load(libpostal_data.class);
        ProcessBuilder pb = new ProcessBuilder("sh", libpostalData, "download", "all", dataDir.toString());
        pb.inheritIO().start().waitFor();

        boolean libpostalSetupCore = postal.libpostal_setup_datadir(dataDir.toString());
        boolean libpostalSetupParser = postal.libpostal_setup_parser_datadir(dataDir.toString());
        boolean libpostalSetupLangClassifier = postal.libpostal_setup_language_classifier_datadir(dataDir.toString());

        if (!libpostalSetupCore || !libpostalSetupParser || !libpostalSetupLangClassifier) {
            String errmsg = String.format(
                    "libpostal initialization failure. Core = [%s] Parser = [%s] Language Classifier = [%s]",
                    libpostalSetupCore ? "SUCCESS" : "FAIL",
                    libpostalSetupParser ? "SUCCESS" : "FAIL",
                    libpostalSetupLangClassifier ? "SUCCESS" : "FAIL");
            throw new UnsatisfiedDependencyException(null, "AddressParser", "libpostal", errmsg);
        }

        this.addressParserOptions = postal.libpostal_get_address_parser_default_options();
        this.normalizeOptions = postal.libpostal_get_default_options();
    }

    public void shutdown() {
        postal.libpostal_teardown();
        postal.libpostal_teardown_parser();
        postal.libpostal_teardown_language_classifier();
    }

    public String[] expand(String addressString) {
        if (addressString == null) {
            throw new NullPointerException("Address string must not be null.");
        }

        log.debug("Expanding {}", addressString);
        try (BytePointer address = new BytePointer(addressString);
                SizeTPointer sizeTPointer = new SizeTPointer(0);
                PointerPointer result = postal.libpostal_expand_address(address, normalizeOptions, sizeTPointer)) {
            long numResults = sizeTPointer.get(0);

            String[] expansions;
            if (Pointer.isNull(result)) {
                log.debug("{} expansions produced", numResults);

                int numResultsInt;
                try {
                    numResultsInt = Math.toIntExact(numResults);
                } catch (ArithmeticException e) {
                    // This should really never happen, but let's just be extra safe
                    if (log.isDebugEnabled()) {
                        log.error("Overflow converting number of expansions to int. Will only return as many as able.",
                                e);
                    }
                    numResultsInt = Integer.MAX_VALUE;
                }
                expansions = new String[numResultsInt];
                for (int i = 0; i < numResultsInt; i++) {
                    expansions[i] = result.getString(i);
                }
            } else {
                log.debug("Expansions null, returning empty array");
                expansions = new String[0];
            }

            postal.libpostal_expansion_array_destroy(result, numResults);
            return expansions;
        }
    }

    public String expandOne(String addressString) {
        if (addressString == null) {
            throw new NullPointerException("Address string must not be null.");
        }

        log.debug("Expanding {}", addressString);
        try (BytePointer address = new BytePointer(addressString);
                SizeTPointer sizeTPointer = new SizeTPointer(0);
                PointerPointer result = postal.libpostal_expand_address(address, normalizeOptions, sizeTPointer)) {
            long numResults = sizeTPointer.get(0);

            String expansion;
            if (!Pointer.isNull(result)) {
                log.debug("{} expansions produced, returning first", numResults);
                expansion = result.getString(0);
            } else {
                log.debug("Expansions null, returning null");
                expansion = null;
            }

            postal.libpostal_expansion_array_destroy(result, numResults);
            return expansion;
        }
    }

    public Address parse(String addressString) {
        if (addressString == null) {
            log.error("Address string must not be null");
            throw new NullPointerException("Address string must not be null.");
        }

        log.debug("Parsing {}", addressString);

        try (BytePointer addressPtr = new BytePointer(addressString);
                libpostal_address_parser_response_t parserResponse = postal.libpostal_parse_address(addressPtr,
                        addressParserOptions)) {

            Address address;
            if (!Pointer.isNull(parserResponse)) {
                String house = "";
                String category = "";
                String near = "";
                String houseNumber = "";
                String road = "";
                String unit = "";
                String level = "";
                String staircase = "";
                String entrance = "";
                String poBox = "";
                String postcode = "";
                String suburb = "";
                String cityDistrict = "";
                String city = "";
                String island = "";
                String stateDistrict = "";
                String state = "";
                String countryRegion = "";
                String country = "";
                String worldRegion = "";

                for (int i = 0; i < parserResponse.num_components(); i++) {
                    // https://github.com/openvenues/libpostal/tree/master#parser-labels
                    switch (parserResponse.labels(i).getString()) {
                        case "house":
                            house = parserResponse.components(i).getString();
                            break;
                        case "category":
                            category = parserResponse.components(i).getString();
                            break;
                        case "near":
                            near = parserResponse.components(i).getString();
                            break;
                        case "house_number":
                            houseNumber = parserResponse.components(i).getString();
                            break;
                        case "road":
                            road = parserResponse.components(i).getString();
                            break;
                        case "unit":
                            unit = parserResponse.components(i).getString();
                            break;
                        case "level":
                            level = parserResponse.components(i).getString();
                            break;
                        case "staircase":
                            staircase = parserResponse.components(i).getString();
                            break;
                        case "entrance":
                            entrance = parserResponse.components(i).getString();
                            break;
                        case "po_box":
                            poBox = parserResponse.components(i).getString();
                            break;
                        case "postcode":
                            postcode = parserResponse.components(i).getString();
                            break;
                        case "suburb":
                            suburb = parserResponse.components(i).getString();
                            break;
                        case "city_district":
                            cityDistrict = parserResponse.components(i).getString();
                            break;
                        case "city":
                            city = parserResponse.components(i).getString();
                            break;
                        case "island":
                            island = parserResponse.components(i).getString();
                            break;
                        case "state_district":
                            stateDistrict = parserResponse.components(i).getString();
                            break;
                        case "state":
                            state = parserResponse.components(i).getString();
                            break;
                        case "country_region":
                            countryRegion = parserResponse.components(i).getString();
                            break;
                        case "country":
                            country = parserResponse.components(i).getString();
                            break;
                        case "world_region":
                            worldRegion = parserResponse.components(i).getString();
                            break;
                        default:
                            break;
                    }
                }

                address = new Address(house, category, near, houseNumber, road, unit, level, staircase, entrance, poBox,
                        postcode, suburb, cityDistrict, city, island, stateDistrict, state, countryRegion, country,
                        worldRegion);
            } else {
                log.debug("Parser response null, returning null");
                address = null;
            }

            postal.libpostal_address_parser_response_destroy(parserResponse);
            return address;
        }
    }
}
