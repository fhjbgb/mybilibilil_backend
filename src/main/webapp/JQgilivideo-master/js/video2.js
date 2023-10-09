
window.onload = function () {

    //初始化
    var id = window.location.href.split("=")[1];
    $.get("http://www.qx.kelasong.cn/api/v1/video/Av"+id,
        function(data){
            var vurl = data.data.url;
            var titl = data.data.title;
            var views =  data.data.view;
            var stars =data.data.star;
            var pid = data.data.uid;
            var video = $('#video1').videoCt({
                title: titl,              //标题
                volume: 0.2,                //音量
                barrage: false,              //弹幕开关
                comment: true,              //弹幕
                reversal: true,             //镜像翻转
                playSpeed: true,            //播放速度
                update: true,               //下载
                autoplay: false,            //自动播放
                clarity: {
                    type: ['标清'], 
                    src:[vurl]           //清晰度
                    // src: ['https://blz-videos.nosdn.127.net/1/OverWatch/OVR-S03_E03_McCree_REUNION_zhCN_1080P_mb78.mp4',
                    //       'https://blz-videos.nosdn.127.net/1/OverWatch/AnimatedShots/Overwatch_AnimatedShot_Mei_RiseAndShine.mp4']      //链接地址
                },
            });
        
            //扩展
            video.title;                    //标题
            video.status;                   //状态
            video.currentTime;              //当前时长
            video.duration;                 //总时长
            video.volume;                   //音量
            video.clarityType;              //清晰度
            video.claritySrc;               //链接地址
            video.fullScreen;               //全屏
            video.reversal;                 //镜像翻转
            video.playSpeed;                //播放速度
            video.cutover;                  //切换下个视频是否自动播放
            video.commentTitle;             //弹幕标题
            video.commentId;                //弹幕id
            video.commentClass;             //弹幕类型
            video.commentSwitch;            //弹幕是否打开
            $("#title-vid").text(titl);
            $("#vid-view-num").text(views+"次播放") ;
            $("#starnums").text(stars)
			$.ajaxSettings.async = false;
            $.get("http://www.qx.kelasong.cn/api/v1/poster/"+pid,
            function(data){
                var pname = data.data.username;
                var timepu = data.data.created_at;
				var avatar = data.data.avatar;
                $("#pname").text(pname);
				$("#pavatar").attr('src',avatar);
                var newDate = new Date(); 
                newDate.setTime(timepu*1000);
                $("#timeup").text("发布于："+newDate.toLocaleString());
            })
            
        },
        );   
    $("#commentbehave").click(function(){
            //初始化
            console.log("nihao");
            var id = window.location.href.split("=")[1];
            console.log(id);
            var text = $("#commenttext").val();
            var data ={"content":text};
            console.log(text);
        $.ajax({
                url:"http://www.qx.kelasong.cn/api/v1/video/comment/Av"+id,
                type:"post",
                contentType: "application/json; charset=utf-8",
                xhrFields:{withCredentials:true},
                processData: false,
                data:JSON.stringify(data),
                dataType: "json",
                success:function(data){
                if (data.status==200){
                    alert("评论成功");
                    location.reload();}
                },
                error:function(e){
                    alert("评论失败");
                    location.reload();
                }   
            });
        }) ;
    $("#star").click(function(){
            var id = window.location.href.split("=")[1];
            var data = {"id":Number(id)};
        $.ajax({
                    url:"http://www.qx.kelasong.cn/api/v1/star",
                    type:"post",
                    contentType: "application/json; charset=utf-8",
                    processData: false,
                    data:JSON.stringify(data),
                    dataType: "json",
                    success:function(data){
                if (data.status==200){
                    alert("点赞成功");
                    $("#starnums").text(Number($("#starnums").text())+1);
            }else{
                    window.location="single_video_fullwidth_page.html?AV="+id;}
                    },
            });
        });
    $("#lstar").click(function(){
            var id = window.location.href.split("=")[1];
            var data = {"id":Number(id)};
            $.ajax({
                    url:"http://www.qx.kelasong.cn/api/v1/lstar",
                    type:"post",
                    contentType: "application/json; charset=utf-8",
                    processData: false,
                    data:JSON.stringify(data),
                    dataType: "json",
                    success:function(data){
                if (data.status==200){
                        alert("点踩成功");
            ;}else{
                    window.location="single_video_fullwidth_page.html?AV="+id;}
                    },
            });
        });
            
}
