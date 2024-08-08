import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
    const [map, setMap] = useState(null);
    const [markers, setMarkers] = useState([]);
    const [infoWindow, setInfoWindow] = useState(null);

    useEffect(() => {
        const script = document.createElement('script');
        script.src = "https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=avh0jngjl9&submodules=geocoder,panorama";
        script.async = true;
        script.onload = () => {
            const position = new naver.maps.LatLng(37.3595704, 127.105399);
            const mapInstance = new naver.maps.Map('map', {
                center: position,
                zoom: 15
            });

            const markerInstance = new naver.maps.Marker({
                position: position,
                map: mapInstance
            });

            const infoWindowInstance = new naver.maps.InfoWindow({
                anchorSkew: true
            });

            setMap(mapInstance);
            setMarkers([markerInstance]);
            setInfoWindow(infoWindowInstance);

            naver.maps.Event.addListener(mapInstance, 'click', function(e) {
                markerInstance.setPosition(e.coord);
                searchCoordinateToAddress(e.coord, infoWindowInstance, mapInstance);
            });

            naver.maps.Event.addListener(mapInstance, 'zoom_changed', function() {
                updateMarkers(mapInstance, markers);
            });

            naver.maps.Event.addListener(mapInstance, 'dragend', function() {
                updateMarkers(mapInstance, markers);
            });
        };
        document.head.appendChild(script);
    }, []);

    const searchAddressToCoordinate = (address) => {
        return new Promise((resolve, reject) => {
            naver.maps.Service.geocode({
                query: address
            }, function(status, response) {
                if (status === naver.maps.Service.Status.ERROR) {
                    alert('Something Wrong!');
                    return reject('Geocode Error');
                }

                if (response.v2.meta.totalCount === 0) {
                    alert('No results found');
                    return reject('No results found');
                }

                const item = response.v2.addresses[0];
                const point = new naver.maps.Point(item.x, item.y);
                const htmlAddresses = [
                    item.roadAddress ? '[도로명 주소] ' + item.roadAddress : '',
                    item.jibunAddress ? '[지번 주소] ' + item.jibunAddress : '',
                    item.englishAddress ? '[영문명 주소] ' + item.englishAddress : ''
                ].filter(Boolean).join('<br />');

                infoWindow.setContent(`
                    <div style="padding:10px;min-width:200px;line-height:150%;">
                        <h4 style="margin-top:5px;">검색 주소 : ${address}</h4><br />
                        ${htmlAddresses}
                    </div>
                `);

                map.setCenter(point);
                infoWindow.open(map, point);

                resolve({ address: htmlAddresses, point, lat: point.y, lng: point.x });
            });
        });
    };

    const handleSearch = async () => {
        const address = document.getElementById('name').value;
        try {
            const result = await searchAddressToCoordinate(address);
            console.log('Latitude:', result.lat); // lat 값 출력
            console.log('Longitude:', result.lng); // lng 값 출력
            markers[0].setPosition(new naver.maps.LatLng(result.lat, result.lng));

            const response = await fetch('http://localhost:8080/map/getChargerInfo', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ lat: result.lat, lng: result.lng })
            });

            const data = await response.json();
            addMarkers(data);
            console.log(data);
        } catch (error) {
            console.error('Error:', error);
        }
    };

    const addMarkers = (data) => {
        // Clear existing markers
        markers.forEach(marker => marker.setMap(null));

        // Create new markers from data
        var newMarkers = data.map(item => {
            const marker = new naver.maps.Marker({
                position: new naver.maps.LatLng(item.latitude, item.longitude),
                map: map
            });
            naver.maps.Event.addListener(marker, 'click', () => {
                infoWindow.setContent(`
                    <div style="padding:10px;min-width:200px;line-height:150%;">
                        <h4 style="margin-top:5px;">Charger Info</h4>
                        <p>충전소: ${item.chargerName}</p>
                        <p>주소: ${item.address}</p>
                        <p>충전기 타입: ${item.chgerType}</p>
                        <p>거리: ${item.distance} km</p>
                        <p>주차요금: ${item.parkingFee}</p>
                        <p>사용가능 시간: ${item.useTime}</p>
                    </div>
                `);
                infoWindow.open(map, marker);
            });
            return marker;
        });

        setMarkers(newMarkers);
    };

    const updateMarkers = (map, markers) => {
        const mapBounds = map.getBounds();
        markers.forEach(marker => {
            const position = marker.getPosition();
            if (mapBounds.hasLatLng(position)) {
                showMarker(map, marker);
            } else {
                hideMarker(map, marker);
            }
        });
    };

    const showMarker = (map, marker) => {
        if (!marker.getMap()) {
            marker.setMap(map);
        }
    };

    const hideMarker = (map, marker) => {
        if (marker.getMap()) {
            marker.setMap(null);
        }
    };

    const searchCoordinateToAddress = (latlng, infoWindow, map) => {
        infoWindow.close();
        console.log(latlng);

        naver.maps.Service.reverseGeocode({
            coords: latlng,
            orders: [
                naver.maps.Service.OrderType.ADDR,
                naver.maps.Service.OrderType.ROAD_ADDR
            ].join(',')
        }, function(status, response) {
            var items = response.v2.results,
                address = '',
                htmlAddresses = [];

            for (var i = 0, ii = items.length, item, addrType; i < ii; i++) {
                item = items[i];
                address = makeAddress(item) || '';
                addrType = item.name === 'roadaddr' ? '[도로명 주소]' : '[지번 주소]';

                htmlAddresses.push((i + 1) + '. ' + addrType + ' ' + address);
            }

            infoWindow.setContent([
                '<div style="padding:10px;min-width:200px;line-height:150%;">',
                '<h4 style="margin-top:5px;">검색 좌표</h4><br />',
                htmlAddresses.join('<br />'),
                '</div>'
            ].join('\n'));

            infoWindow.open(map, latlng);
        });
    };

    const makeAddress = (item) => {
        if (!item) {
            return;
        }

        var name = item.name,
            region = item.region,
            land = item.land,
            isRoadAddress = name === 'roadaddr';

        var sido = '', sigugun = '', dongmyun = '', ri = '', rest = '';

        if (hasArea(region.area1)) {
            sido = region.area1.name;
        }

        if (hasArea(region.area2)) {
            sigugun = region.area2.name;
        }

        if (hasArea(region.area3)) {
            dongmyun = region.area3.name;
        }

        if (hasArea(region.area4)) {
            ri = region.area4.name;
        }

        if (land) {
            if (hasData(land.number1)) {
                if (hasData(land.type) && land.type === '2') {
                    rest += '산';
                }

                rest += land.number1;

                if (hasData(land.number2)) {
                    rest += ('-' + land.number2);
                }
            }

            if (isRoadAddress === true) {
                if (checkLastString(dongmyun, '면')) {
                    ri = land.name;
                } else {
                    dongmyun = land.name;
                    ri = '';
                }

                if (hasAddition(land.addition0)) {
                    rest += ' ' + land.addition0.value;
                }
            }
        }

        return [sido, sigugun, dongmyun, ri, rest].join(' ');
    };

    const hasArea = (area) => {
        return !!(area && area.name && area.name !== '');
    };

    const hasData = (data) => {
        return !!(data && data !== '');
    };

    const checkLastString = (word, lastString) => {
        return new RegExp(lastString + '$').test(word);
    };

    const hasAddition = (addition) => {
        return !!(addition && addition.value);
    };

    return (
        <>
            <div id="address-form">
                <label htmlFor="name">도로명 주소 입력:</label>
                <input type="search" id="name" name="name" minLength="2" required size="10" />
                <button id="submit" onClick={handleSearch}>검색</button>
            </div>
            <div id="map" style={{ width: '100%', height: '100vh' }}></div>
        </>
    );
}

export default App;
