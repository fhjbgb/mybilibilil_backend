$(function() {
     var cookie=document.cookie;
     if (cookie.length !=0){
         $("#section1").hide()
     };
    $.get(
        "http://www.qx.kelasong.cn/api/v1/videos",
        function(data){
            var img1 = data.data.items[0].avatar;
            var img2 = data.data.items[1].avatar;
            var img3 = data.data.items[2].avatar;
            var img4 = data.data.items[3].avatar;
            var img5 = data.data.items[4].avatar;
            var img6 = data.data.items[5].avatar;
            var img7 = data.data.items[6].avatar;
            var img8 = data.data.items[7].avatar;

            var video1 = data.data.items[0].id;
            var video2 = data.data.items[1].id;
            var video3 = data.data.items[2].id;
            var video4 = data.data.items[3].id;
            var video5 = data.data.items[4].id;
            var video6 = data.data.items[5].id;
            var video7 = data.data.items[6].id;
            var video8 = data.data.items[7].id;

            var title1 = data.data.items[0].title;
            var title2 = data.data.items[1].title;
            var title3 = data.data.items[2].title;
            var title4 = data.data.items[3].title;
            var title5 = data.data.items[4].title;
            var title6 = data.data.items[5].title;
            var title7 = data.data.items[6].title;
            var title8 = data.data.items[7].title;

            var info1 = data.data.items[0].Info;
            var info2 = data.data.items[1].Info;
            var info3 = data.data.items[2].Info;
            var info4 = data.data.items[3].Info;
            var info5 = data.data.items[4].Info;
            var info6 = data.data.items[5].Info;
            var info7 = data.data.items[6].Info;
            var info8 = data.data.items[7].Info;
            

            $("#imgs1").attr('src',img1);
            $("#imgs2").attr('src',img2);
            $("#imgs3").attr('src',img3);
            $("#imgs4").attr('src',img4);
            $("#imgs5").attr('src',img5);
            $("#imgs6").attr('src',img6);
            $("#imgs7").attr('src',img7);
            $("#imgs8").attr('src',img8);

            $("#videos1").attr('href',"single_video_fullwidth_page.html?AV="+video1);
            $("#videos2").attr('href',"single_video_fullwidth_page.html?AV="+video2);
            $("#videos3").attr('href',"single_video_fullwidth_page.html?AV="+video3);
            $("#videos4").attr('href',"single_video_fullwidth_page.html?AV="+video4);
            $("#videos5").attr('href',"single_video_fullwidth_page.html?AV="+video5);
            $("#videos6").attr('href',"single_video_fullwidth_page.html?AV="+video6);
            $("#videos7").attr('href',"single_video_fullwidth_page.html?AV="+video7);
            $("#videos8").attr('href',"single_video_fullwidth_page.html?AV="+video8);
           
            $("#titl1").text(title1);
            $("#titl2").text(title2);
            $("#titl3").text(title3);
            $("#titl4").text(title4);
            $("#titl5").text(title5);
            $("#titl6").text(title6);
            $("#titl7").text(title7);
            $("#titl8").text(title8);

            $("#info1").text(info1);
            $("#info2").text(info2);
            $("#info3").text(info3);
            $("#info4").text(info4);
            $("#info5").text(info5);
            $("#info6").text(info6);
            $("#info7").text(info7);
            $("#info8").text(info8);
        },
    );
})
$.get(
    "http://www.qx.kelasong.cn/api/v1/hotvideos",
    function(data){
        var img9 = data.data[0].avatar;
        var img10 = data.data[1].avatar;
        var img11 = data.data[2].avatar;
        var img12 = data.data[3].avatar;
        var img13 = data.data[4].avatar;
        var img14 = data.data[5].avatar;
        var img15 = data.data[6].avatar;
        var img16 = data.data[7].avatar;

        var video9 = data.data[0].id;
        var video10 = data.data[1].id;
        var video11 = data.data[2].id;
        var video12 = data.data[3].id;
        var video13 = data.data[4].id;
        var video14 = data.data[5].id;
        var video15 = data.data[6].id;
        var video16 = data.data[7].id;

        var title9 = data.data[0].title;
        var title10 = data.data[1].title;
        var title11 = data.data[2].title;
        var title12 = data.data[3].title;
        var title13 = data.data[4].title;
        var title14 = data.data[5].title;
        var title15 = data.data[6].title;
        var title16 = data.data[7].title;
    

        var info9 = data.data[0].Info;
        var info10 = data.data[1].Info;
        var info11 = data.data[2].Info;
        var info12 = data.data[3].Info;
        var info13 = data.data[4].Info;
        var info14 = data.data[5].Info;
        var info15 = data.data[6].Info;
        var info16 = data.data[7].Info;
        

        $("#imgs9").attr('src',img9);
        $("#imgs10").attr('src',img10);
        $("#imgs11").attr('src',img11);
        $("#imgs12").attr('src',img12);
        $("#imgs13").attr('src',img13);
        $("#imgs14").attr('src',img14);
        $("#imgs15").attr('src',img15);
        $("#imgs16").attr('src',img16);

        $("#videos9").attr('href',"single_video_fullwidth_page.html?AV="+video9);
        $("#videos10").attr('href',"single_video_fullwidth_page.html?AV="+video10);
        $("#videos11").attr('href',"single_video_fullwidth_page.html?AV="+video11);
        $("#videos12").attr('href',"single_video_fullwidth_page.html?AV="+video12);
        $("#videos13").attr('href',"single_video_fullwidth_page.html?AV="+video13);
        $("#videos14").attr('href',"single_video_fullwidth_page.html?AV="+video14);
        $("#videos15").attr('href',"single_video_fullwidth_page.html?AV="+video15);
        $("#videos16").attr('href',"single_video_fullwidth_page.html?AV="+video16);
       
        $("#titl9").text(title9);
        $("#titl10").text(title10);
        $("#titl11").text(title11);
        $("#titl12").text(title12);
        $("#titl13").text(title13);
        $("#titl14").text(title14);
        $("#titl15").text(title15);
        $("#titl16").text(title16);

        $("#info9").text(info9);
        $("#info10").text(info10);
        $("#info11").text(info11);
        $("#info12").text(info12);
        $("#info13").text(info13);
        $("#info14").text(info14);
        $("#info15").text(info15);
        $("#info16").text(info16);
    })
$("#searchbutton").click(function() { 
        var param = document.getElementById("searchtext").value;
        window.location="Searched_Videos_page.html?Param="+param;
 })
   