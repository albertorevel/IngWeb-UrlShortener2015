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
                    type : "GET",
                    url : "/publicidad",
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
    });