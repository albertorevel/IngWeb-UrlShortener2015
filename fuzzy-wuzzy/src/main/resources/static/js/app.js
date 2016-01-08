function initMap() {
    $.ajax({
        type: "GET",
        url: "/map",
        data: $(this).serialize(),
        success: function (msg) {
            var punto = new google.maps.LatLng(41.64886959999999, -0.889742100000035);
            var myOptions = {
                center: punto,
                zoom: 4
            };
            var map = new google.maps.Map(document.getElementById('map'), myOptions);
            var h = map.
            $.each(msg, function (i, item) {
                var puntoMarc = new google.maps.LatLng(item.latitud, item.longitud);
                var marker = new google.maps.Marker({
                    position:puntoMarc,
                    map: map,
                    title:"IP: "+item.ip});
            });
        },
        error: function () {
            var punto = new google.maps.LatLng(41.64886959999999, -0.889742100000035);
            var myOptions = {
                center: punto,
                zoom: 4
            };
            var map = new google.maps.Map(document.getElementById('map'), myOptions);
        }
    });
};
$(document).ready(
    function() {
        $("#shortener").submit(
            function(event) {
                event.preventDefault();
                $.ajax({
                    type : "POST",
                    url : "/link",
                    data : $(this).serialize(),
                    success : function(msg) {
                        $("#result").html(
                            "<div class='alert alert-success lead'><a target='_blank' href='"
                            + msg.uri
                            + "'>"
                            + msg.uri
                            + "</a></div><br/></div>"
                            +"<div tiles:qr>"
                            +"<img src='data:image/png;base64, "
                            + msg.qrCode
                            + "' alt = 'Red Dot'/> "
                            + msg.qrApi+"</div>");
                    },
                    error : function() {
                        $("#result").html(
                                "<div class='alert alert-danger lead'>ERROR</div>");
                    }
                });
            });
        $("#info").submit(
            function(event) {
                event.preventDefault();
                $.ajax({
                    type : "POST",
                    url : "/info",
                    data : $(this).serialize(),
                    success : function(msg) {
                        var groups = document.getElementsByName("group");
                        var selected = "";

                        for(var i = 0; i < groups.length; i++) {
                            if(groups[i].checked == true) {
                                selected = groups[i].value;
                            }
                        }
                        var trHTML = '';
                        if(selected == "country") {
                            trHTML += '<tr> <th>Country</th> <th>Target</th>  <th>Count</th> </tr>'
                            $.each(msg, function (i, item) {

                                trHTML += '<tr><td>' + item.country + '</td><td>' + item.target + '</td><td>' + item.count + '</td></tr>';
                            });
                        } else if(selected == "comunity"){
                            trHTML += '<tr> <th>Country</th> <th>Comunity</th> <th>Target</th>  <th>Count</th> </tr>'
                            $.each(msg, function (i, item) {

                                trHTML += '<tr><td>' + item.country + '</td><td>' + item.comunity + '</td><td>' + item.target + '</td><td>' + item.count + '</td></tr>';
                            });
                        }
                        else if(selected == "city"){
                            trHTML += '<tr> <th>Country</th> <th>Comunity</th> <th>City</th> <th>Target</th>  <th>Count</th> </tr>'
                            $.each(msg, function (i, item) {

                                trHTML += '<tr><td>' + item.country + '</td><td>' + item.comunity + '</td>' +
                                    '<td>' + item.city + '</td><td>' + item.target + '</td><td>' + item.count + '</td></tr>';
                            });
                        }
                        $('#tabla').html(trHTML);
                        },
                    error : function() {
                        $("#tabla").html(
                            "<div class='alert alert-danger lead'>ERROR</div>");
                    }
                });
            });
    });