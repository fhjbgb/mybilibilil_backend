$("#btn").click(function() { 
    var Username=document.getElementById("username").value;
    var Password=document.getElementById("password").value;
    var obj = {
        "username":Username,
        "password":Password,
    }
    $.ajax({
        url:"http://www.qx.kelasong.cn/api/v1/login",
        type:"post",
        contentType: "application/json; charset=utf-8",
        processData: false,
        xhrFields:{withCredentials:true},
        crossDomain:true,
        data:JSON.stringify(obj),
        dataType: "json",
        success:function(data){
            if(data.status==200){
				document.cookie = "username = "+data.data.username;
				sessionStorage.setItem("user",JSON.stringify(data.data));
                 window.location.href="index.html";
            }else if(data.status==40001){
                alert(data.msg+"请注册！")
                
            }else{
                alert(data.msg+"请输入正确的账号密码！")
            }
        },
        error:function(e){
        console.log(data+"请求失败，请重新尝试！");
        
        }
    });
});