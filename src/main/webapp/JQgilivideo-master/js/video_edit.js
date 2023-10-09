$(function(){
    var fileName = "";
    var file="";
    $("#file-upload").change(function(){
        var filestream = $("#file-upload").get(0).files[0];
        file = $("#file-upload").val();
        fileName = file.split("\\").pop();
        var obj={
            "fileavatarname":fileName
        }
        $.ajax({
            url:"http://www.qx.kelasong.cn/api/v1/avatartoken",
            type:"post",
            contentType: "application/json; charset=utf-8",
            processData: false,
            async: false,
            xhrFields:{withCredentials:true},
            crossDomain:true,
            data:JSON.stringify(obj),
            dataType: "json",
            success:function(data){
                console.log(data);
                if(data.status==0){
                    avatarPutUrl = data.data.put;
                    avatarGetUrl = data.data.get;
                    avatarKey = data.data.key;
                    $.ajax({
                        url:avatarPutUrl,
                        type:"put",
                        contentType: "image/jpeg",
                        processData: false,
						xhrFields:{withCredentials:false},
                        async: false,
                        data:filestream,
                        success:function(data){
                            $("#image-avatar").attr("src",avatarGetUrl); 
                            sessionStorage.setItem("avatarkey",avatarKey);
                    },
                    error:function(e){
                            console.log("图片显示失败！");
                    }
                }) 
                }
                else{
                    console.log(data.data+"上传失败服务器问题");
                }
            },
            error:function(e){
                console.log("上传失败！请刷新页面重新上传！");
            }
        })
    })
    $("#btn").click(function(){
        var title = $("#video-title").val();
        var info = $("#video-info").val();
        var videoUrl = sessionStorage.getItem("videokey");
        var avatarUrl=sessionStorage.getItem("avatarkey");
        var files = {
            "title":title,
            "info":info,
            "avatar":avatarUrl,
            "path":videoUrl,
        }
        console.log(files);
        
        $.ajax({
            url:"http://www.qx.kelasong.cn/api/v1/video",
            type:"post",
            contentType: "application/json; charset=utf-8",
            processData: false,
            async: false,
            xhrFields:{withCredentials:true},
            crossDomain:true,
            data:JSON.stringify(files),
            dataType:"json",
            success:function(data){
                console.log(data.msg);
                if(data.status==200){
                alert("上传完成，点击确定页面跳转！")
                window.location.href = "index.html"
                }else{
                alert("上传失败！" + data.msg)
                }
            },
            error:function(e){
                console.log("失败"); 
            },
        })
    }) ;
})