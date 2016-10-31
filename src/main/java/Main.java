import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.api_entities.ComponentData;
import model.api_entities.ComponentInformation;
import model.api_entities.StationData;
import model.api_entities.StationInformation;
import model.output_entities.Component;
import model.output_entities.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MINUTES;


class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final RestTemplate restTemplate = new RestTemplate();

    private static final String URI = "http://hamburg.luftmessnetz.de/api";
    private static final String ACTIVE_STATIONS = "/station/overview/active?_format=json";
    private static final String COMPONENT_OVERVIEW = "/component/overview/pollution?_format=json";
    private static final String COMPONENT_BY_CODE = "/component/%s?_format=json";


    //Benötigte Komponenten
    private static final List<String> N_COMPONENTS = Arrays.asList("NO", "NO2", "PM10");

    private static List<StationInformation> stationInformations = new ArrayList<>();
    private static List<ComponentInformation> componentInformations = new ArrayList<>();


    public static void main(String[] args) {
        log.debug("Start application");
        //hole alle Stationsinformationen von der Api
        stationInformations = Arrays.asList(getStationInformations());

        //hole alle Komponenteninformationen von der Api
        componentInformations = Arrays.asList(getComponentInformations());

        final Runnable executeWorkflow = () -> {
            //Hole Daten
            List<Station> out = gatherData();
            //TODO: Schreibe Daten in Kafka

            try {
                log.debug(mapper.writeValueAsString(out));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        };

        //wiederhole worflow alle 60 Minuten
        scheduler.scheduleAtFixedRate(executeWorkflow, 0L, 60L, MINUTES);

    }

    private static List<Station> gatherData(){
        List<Map<String, StationData>> stationDataList = new ArrayList<>();

        //hole alle Daten für die benötigten Komponenten
        N_COMPONENTS.forEach(c -> stationDataList.add(getDataByComponent(c)));

        AtomicInteger stationId = new AtomicInteger(1);
        List<Station> output = new ArrayList<>();

        //Erstelle Objekte für den Output
        stationInformations.forEach(s -> {
            //Iteriere durch alle bekannten Stationen
            Station outStation = new Station();
            outStation.setId(stationId.getAndIncrement());
            outStation.setName(s.getName());
            outStation.setAddress(s.getAddress());
            outStation.setLat(s.getLat());
            outStation.setLng(s.getLng());
            outStation.setTimestamp(new Date().getTime());

            List<Component> tmpComponents = new ArrayList<>();
            //Iteriere durch die geladenen Komponenten-Daten
            stationDataList.forEach(stationDataMap -> {
                StationData stationData = stationDataMap.get(outStation.getName());
                int i = stationDataList.indexOf(stationDataMap);
                String componentName = N_COMPONENTS.get(i);
                if (stationData != null) {
                    //Erstelle eine Komponente und befülle diese mit dem neuesten Wert, sowie dem neusten Zeitstempel
                    Component cmp = new Component();
                    cmp.setName(componentName);
                    stationData.getData().forEach((k, v) -> {
                        if (k > cmp.getTimestamp()) {
                            cmp.setTimestamp(k * 1000);
                            cmp.setValue(v);
                        }
                    });
                    cmp.setUnit(getUnitByCode(componentName));
                    tmpComponents.add(cmp);
                }
            });
            outStation.setComponents(tmpComponents);
            output.add(outStation);
        });
        return output;
    }


    private static ComponentInformation getComponentByCode(String name) {
        //hole passende Komponente aus der komponenten Liste
        return componentInformations.stream().filter(c -> c.getCode().equals(name)).collect(Collectors.toList()).get(0);
    }

    private static String getUnitByComponent(ComponentInformation componentInformation) {
        return Arrays.stream(componentInformation.getComponents()).filter(sc -> sc.getCode().equals("24h_" + componentInformation.getCode())).map(ComponentData::getUnit).collect(Collectors.toList()).get(0);
    }
    private static String getUnitByCode(String code) {
        ComponentInformation componentInformation = getComponentByCode(code);
        return Arrays.stream(componentInformation.getComponents()).filter(sc -> sc.getCode().equals("24h_" + componentInformation.getCode())).map(ComponentData::getUnit).collect(Collectors.toList()).get(0);
    }

    private static StationInformation[] getStationInformations() {
        //hole alle Stations-Informationen von der Api
        return restTemplate.getForObject(URI + ACTIVE_STATIONS, StationInformation[].class);
    }

    private static ComponentInformation[] getComponentInformations() {
        //hole alle Komponenten-Informationen von der Api
        return restTemplate.getForObject(URI + COMPONENT_OVERVIEW, ComponentInformation[].class);
    }


    private static Map<String, StationData> getDataByComponent(String componentName) {
        String response = restTemplate.getForObject(URI + String.format(COMPONENT_BY_CODE, componentName), String.class);

        //Weil der Depp, der die Schnittstelle geschrieben hat, gerne mal eine Leere liste dort hinschreibt wo eigentlich eine Map sein sollte, müssen
        //alle leeren listen zu leeren Objekten umgewandelt werden
        response = response.replace("[]", "{}");
        Map<String, StationData> map = new HashMap<>();
        try {
            map = mapper.readValue(response, new TypeReference<HashMap<String, StationData>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

}
