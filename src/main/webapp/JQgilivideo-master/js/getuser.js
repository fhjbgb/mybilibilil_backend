$(function(){
    var cookie=document.cookie;
     if (cookie.length !=0){
		$.ajaxSetup({
			crossDomain:true,
			xhrFields:{
				withCredentials:true
			}
		});
        $.get("http://www.qx.kelasong.cn/api/v1/userinfo",
        function(data){
            var username = data.data.username;
            var avatar = data.data.avatar;
            $("[id = uuuavatar]").attr('src',avatar);
            $("[id = uuuname]").text(username);
        })
     };    
})
 