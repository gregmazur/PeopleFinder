window.people_network_UI_client_RPCaller =    function() {
        var connector = this;

        // add a method to the connector
        this.makeRequest = function(theUrl){
            var xmlHttp = new XMLHttpRequest();
            xmlHttp.onreadystatechange = function() {
                if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
                    connector.returnResponse(xmlHttp.responseText);
            }
        };
        xmlHttp.open("GET", theUrl, false); // true for asynchronous
        xmlHttp.send(null);
    };
