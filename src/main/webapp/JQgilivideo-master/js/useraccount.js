/**
 * 头像服务AVATAR
 */
$(function(){
    var fileName = "";
    var file="";
    $("#avatar-upload").change(function(){
        var filestream = $("#avatar-upload").get(0).files[0];
        file = $("#avatar-upload").val();
        fileName = file.split("\\").pop();
        var filn={
            "fileavatarname":fileName
        }
        $.ajax({
            url:"http://www.qx.kelasong.cn/api/v1/authavatartoken",
            type:"post",
            contentType: "application/json; charset=utf-8",
            processData: false,
            async: false,
            xhrFields:{withCredentials:true},
            crossDomain:true,
            data:JSON.stringify(filn),
            xhrFiled:{
                'withCredentials':true
                },
            crossDomain:true,
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
                        data:filestream,
                        success:function(data){
                            $("#image-avatar").attr("src",avatarGetUrl); 
                            sessionStorage.setItem("authavatarkey",avatarKey);
                    
                },
                    error:function(e){
                            alert("图片显示失败！");
                    }
                }) 
                }else{
                    console.log(data.data+"上传失败服务器问题");
                };
            },
            error:function(e){
                console.log("上传失败！请刷新页面重新上传！");
            },
        })
    })
    $("#avatar-btn").click(function(){
        var filen = sessionStorage.getItem("authavatarkey");
		var avatar ={"avatar":filen}
        $.ajax({
            url:"http://www.qx.kelasong.cn/api/v1/upavatar",
            type:"post",
            contentType: "application/json; charset=utf-8",
            processData: false,
            async: false,
            xhrFields:{withCredentials:true},
            crossDomain:true,
            data:JSON.stringify(avatar),
            xhrFiled:{
                'withCredentials':true
                },
            crossDomain:true,
            dataType: "json",
            success:function(data){
                if (data.status==200){
window.location.reload();}else{
alert("请上传正确的图片格式即jpg图片")}
            } ,
            error:function(){
                alert("更新失败")
            } 
        })
    })
})