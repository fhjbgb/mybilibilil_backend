$(function() {
	var fileName = "";
	var file = "";
	$("#file-upload").change(function() {
		var filestream = $("#file-upload").get(0).files[0];
		file = $("#file-upload").val();
		fileName = file.split("\\").pop();
		var obj = {
			"filevideoname": fileName
		}
		$.ajax({
			url: "/api/v1/videotoken",
			type: "post",
			contentType: "application/json; charset=utf-8",
			processData: false,
			async: false,
			xhrFields: {
				withCredentials: true
			},
			crossDomain: true,
			data: JSON.stringify(obj),
			dataType: "json",
			success: function(data) {
				console.log(data);
				layer.load(2, {
					shade: [0.8, '#fff'] 
				});
				if (data.status == 0) {
					videoPutUrl = data.data.put
					// videoGetUrl = data.data.get
					videoKey = data.data.key

					$.ajax({
						url: videoPutUrl,
						type: "put",
						contentType: "video/mp4",
						async: false,
						data: filestream,
						xhrFields: {
							withCredentials: false
						},
						processData: false,

						success: function(data) {
							console.log("ok");
							sessionStorage.setItem("videokey", videoKey);
							window.location.href = "Upload_Edit.html"
						},
						error: function() {
							console.log("莫名奇妙失败");
						}
					})
				} else {
					alert(data.msg + "!" + "上传失败!服务器问题");
				}
			},
			error: function(e) {
				console.log("上传失败！请刷新页面重新上传！");
			}
		})
	})
})
