package elice.chargingstationbackend.business.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import elice.chargingstationbackend.business.entity.BusinessOwner;
import elice.chargingstationbackend.business.repository.BusinessOwnerRepository;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
@RequiredArgsConstructor
public class ChargeStationService {


    private final BusinessOwnerRepository businessOwnerRepository;


    public void fetchAndSaveChargeStationData() {
        String serviceKey = "ujQYUsjsfyuEAFHXJUvQfibDKiHR77QD4czLDXiJzOTN6FIGAxAAWkZy%2FtIVV%2B%2FlHv%2Bily25gDSSGo29%2FGq18g%3D%3D";
        int pageNo = 1;
        int numOfRows = 100;
        boolean moreData = true;

        while (moreData) {
            String url = "https://apis.data.go.kr/B552584/EvCharger/getChargerInfo?ServiceKey=" + serviceKey + "&pageNo=" + pageNo + "&numOfRows=" + numOfRows + "&dataType=XML";
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();

                List<BusinessOwner> businessOwners = parseBusinessOwnersFromXml(sb.toString());
                businessOwnerRepository.saveAll(businessOwners);

                if (businessOwners.size() < numOfRows) {
                    moreData = false;
                } else {
                    pageNo++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                moreData = false;
            }
        }
    }

    private List<BusinessOwner> parseBusinessOwnersFromXml(String xmlResponse) {
        List<BusinessOwner> businessOwners = new ArrayList<>();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new java.io.StringReader(xmlResponse)));
            NodeList items = doc.getElementsByTagName("item");

            for (int i = 0; i < items.getLength(); i++) {
                org.w3c.dom.Node item = items.item(i);
                BusinessOwner businessOwner = new BusinessOwner();
                businessOwner.setBusinessName(Optional.ofNullable(getValue("busiNm", item)).orElse("Default Business"));
                businessOwner.setBusinessOperatorName(Optional.ofNullable(getValue("bnm", item)).orElse("Default Operator"));
                businessOwner.setContactInfo(Optional.ofNullable(getValue("busiCall", item)).orElse("000-0000-0000"));
                businessOwner.setAddress(Optional.ofNullable(getValue("addr", item)).orElse("Default Address"));

                // 기본 이메일 설정
                businessOwner.setOwnerEmail("default@example.com");
                businessOwner.setOwnerName("Default Name");

                businessOwners.add(businessOwner);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return businessOwners;
    }

    private String getValue(String tag, org.w3c.dom.Node item) {
        NodeList nodeList = ((org.w3c.dom.Element) item).getElementsByTagName(tag);
        if (nodeList.getLength() > 0) {
            NodeList nodes = nodeList.item(0).getChildNodes();
            if (nodes != null && nodes.getLength() > 0) {
                return nodes.item(0).getNodeValue();
            }
        }
        return null;
    }
}
