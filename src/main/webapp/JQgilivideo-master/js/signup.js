$("#btn").click(function() { 
    var Username=document.getElementById("username").value;
    var Password=document.getElementById("password").value;
    var Passwordtwo=document.getElementById("passwordtwo").value;
    var Email=document.getElementById("email").value;
    var Brithday=document.getElementById("brithday").value;
    var Gender=$("input[type='radio']:checked").val();
    var obj = {
        username:Username,
        email:Email,
        password:Password,
        passwordtwo:Passwordtwo,
        brithday:Brithday,
        gender:Gender,
    }
    $.ajax({
        type : "POST",
        url : "http://www.qx.kelasong.cn/api/v1/regist",
        contentType : "application/json; charset=utf-8",
        processData : false,
        data : JSON.stringify(obj),
        dataType : "json",
        success : function(data){
            if(data.status==200){
               console.log("接受到消息了");
               window.location.href="login.html";
            }
            else{
                alert(data.msg);
            } 
        },
        error : function(data){
            alert("网络不佳请稍后再试！");
        },
    });
});