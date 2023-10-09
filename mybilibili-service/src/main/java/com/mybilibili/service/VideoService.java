package com.mybilibili.service;

import com.mybilibili.dao.VideoDao;
import com.mybilibili.domain.*;
import com.mybilibili.domain.exception.ConditionException;
import com.mybilibili.service.util.FastDFSUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class VideoService {

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Autowired
    private UserCoinService userCoinService;

    @Autowired
    private UserService userService;

//    @Autowired
//    private ImageUtil imageUtil;

    @Autowired
    private FileService fileService;

    private static final int FRAME_NO = 256;


    //事务处理
    @Transactional
    public void addVideos(Video video) {
        Date now = new Date();
        video.setCreateTime(new Date());
        videoDao.addVideos(video);
        Long videoId = video.getId();
        List<VideoTag> tagList = video.getVideoTagList();
        tagList.forEach(item -> {
            item.setCreateTime(now);
            item.setVideoId(videoId);
        });
        videoDao.batchAddVideoTags(tagList);
    }

    public PageResult<Video> pageListVideos(Integer size, Integer no, String area) {
        if(size == null || no == null){
            throw new ConditionException("参数异常！");
        }
        //用map记录查询的条件
        Map<String, Object> params = new HashMap<>();
        //起始位置
        params.put("start", (no-1)*size);
        //查多少数据
        params.put("limit", size);
        //可能会按分区查询
        params.put("area" , area);
        List<Video> list = new ArrayList<>();
        //计算符合条件的数据有多少条
        Integer total = videoDao.pageCountVideos(params);
        if(total > 0){
            //有数据，继续查实际满足的数据是什么
            list = videoDao.pageListVideos(params);
        }
        return new PageResult<>(total, list);
    }

    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) {
        try{
            //调用fastdfsutil实现功能
            fastDFSUtil.viewVideoOnlineBySlices(request, response, url);
        }catch (Exception ignored){}
    }


    public void addVideoLike(Long videoId, Long userId) {
        //获取视频实体
        Video video = videoDao.getVideoById(videoId);
        //判断情况
        if(video == null){
            throw new ConditionException("非法视频！");
        }
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        if(videoLike != null){
            throw new ConditionException("已经赞过！");
        }
        videoLike = new VideoLike();
        videoLike.setVideoId(videoId);
        videoLike.setUserId(userId);
        videoLike.setCreateTime(new Date());
        videoDao.addVideoLike(videoLike);
    }

    public void deleteVideoLike(Long videoId, Long userId) {
        videoDao.deleteVideoLike(videoId, userId);
    }

    public Map<String, Object> getVideoLikes(Long videoId, Long userId) {
        Long count = videoDao.getVideoLikes(videoId);
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        //判断当前用户是否点赞了
        boolean like = videoLike != null;
        //记录在map中
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);
        return result;
    }

    @Transactional
    public void addVideoCollection(VideoCollection videoCollection, Long userId) {
        //获取对象对应的id并判断
        Long videoId = videoCollection.getVideoId();
        Long groupId = videoCollection.getGroupId();
        if(videoId == null || groupId == null){
            throw new ConditionException("参数异常！");
        }
        Video video = videoDao.getVideoById(videoId);
        if(video == null){
            throw new ConditionException("非法视频！");
        }
        //更新效果实现
        //删除原有视频收藏
        videoDao.deleteVideoCollection(videoId, userId);
        //添加新的视频收藏
        videoCollection.setUserId(userId);
        videoCollection.setCreateTime(new Date());
        videoDao.addVideoCollection(videoCollection);
    }

    public void deleteVideoCollection(Long videoId, Long userId) {
        videoDao.deleteVideoCollection(videoId, userId);
    }

    public Map<String, Object> getVideoCollections(Long videoId, Long userId) {
        Long count = videoDao.getVideoCollections(videoId);
        VideoCollection videoCollection = videoDao.getVideoCollectionByVideoIdAndUserId(videoId, userId);
        boolean like = videoCollection != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);
        return result;
    }

    @Transactional
    public void addVideoCoins(VideoCoin videoCoin, Long userId) {
        Long videoId = videoCoin.getVideoId();
        Integer amount = videoCoin.getAmount();
        if(videoId == null){
            throw new ConditionException("参数异常！");
        }
        Video video = videoDao.getVideoById(videoId);
        if(video == null){
            throw new ConditionException("非法视频！");
        }
        //查询当前登录用户是否拥有足够的硬币
        Integer userCoinsAmount = userCoinService.getUserCoinsAmount(userId);
        userCoinsAmount = userCoinsAmount == null ? 0 : userCoinsAmount;
        if(amount > userCoinsAmount){
            throw new ConditionException("硬币数量不足！");
        }
        //查询当前登录用户对该视频已经投了多少硬币
        VideoCoin dbVideoCoin = videoDao.getVideoCoinByVideoIdAndUserId(videoId, userId);
        //新增视频投币
        if(dbVideoCoin == null){
            videoCoin.setUserId(userId);
            videoCoin.setCreateTime(new Date());
            videoDao.addVideoCoin(videoCoin);
        }else{
            Integer dbAmount = dbVideoCoin.getAmount();
            dbAmount += amount;
            //更新视频投币
            videoCoin.setUserId(userId);
            videoCoin.setAmount(dbAmount);
            videoCoin.setUpdateTime(new Date());
            videoDao.updateVideoCoin(videoCoin);
        }
        //更新用户当前硬币总数
        userCoinService.updateUserCoinsAmount(userId, (userCoinsAmount-amount));
    }

    public Map<String, Object> getVideoCoins(Long videoId, Long userId) {
        Long count = videoDao.getVideoCoinsAmount(videoId);
        VideoCoin videoCollection = videoDao.getVideoCoinByVideoIdAndUserId(videoId, userId);
        boolean like = videoCollection != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);
        return result;
    }

    public void addVideoComment(VideoComment videoComment, Long userId) {
        Long videoId = videoComment.getVideoId();
        if(videoId == null){
            throw new ConditionException("参数异常！");
        }
        Video video = videoDao.getVideoById(videoId);
        if(video == null){
            throw new ConditionException("非法视频！");
        }
        videoComment.setUserId(userId);
        videoComment.setCreateTime(new Date());
        videoDao.addVideoComment(videoComment);
    }

    public PageResult<VideoComment> pageListVideoComments(Integer size, Integer no, Long videoId) {
        Video video = videoDao.getVideoById(videoId);
        if(video == null){
            throw new ConditionException("非法视频！");
        }
        //分页设置
        Map<String, Object> params = new HashMap<>();
        params.put("start", (no-1)*size);
        params.put("limit", size);
        params.put("videoId", videoId);
        //获取该分页的总数据量
        Integer total = videoDao.pageCountVideoComments(params);
        List<VideoComment> list = new ArrayList<>();
        if(total > 0){
            //一级评论中有列表包含二级评论，在数据库中遍历查询消耗大。故在此遍历
            list = videoDao.pageListVideoComments(params);
            //批量查询二级评论
            //取出一级评论的主键id，放到parentIdList中作为根id
            List<Long> parentIdList = list.stream().map(VideoComment::getId).collect(Collectors.toList());
            //通过根id做关联，查询回复/子评论的信息
            List<VideoComment> childCommentList = videoDao.batchGetVideoCommentsByRootIds(parentIdList);
            //批量查询用户信息，然后合并集合，使用set是为了去重
            Set<Long> userIdList = list.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            Set<Long> replyUserIdList = childCommentList.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            Set<Long> childUserIdList = childCommentList.stream().map(VideoComment::getReplyUserId).collect(Collectors.toSet());
            //合并set
            userIdList.addAll(replyUserIdList);
            userIdList.addAll(childUserIdList);
            //遍历集合获得用户信息
            List<UserInfo> userInfoList = userService.batchGetUserInfoByUserIds(userIdList);
            //userId和UserInfo封装在map中，方便获取信息
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo :: getUserId, userInfo -> userInfo));
           //遍历
            list.forEach(comment -> {
                //获取id
                Long id = comment.getId();
                //子评论列表
                List<VideoComment> childList = new ArrayList<>();
                childCommentList.forEach(child -> {
                    //id与根id相关联，赋值并添加
                    if(id.equals(child.getRootId())){
                        child.setUserInfo(userInfoMap.get(child.getUserId()));
                        child.setReplyUserInfo(userInfoMap.get(child.getReplyUserId()));
                        childList.add(child);
                    }
                });
                comment.setChildList(childList);
                comment.setUserInfo(userInfoMap.get(comment.getUserId()));
            });
        }
        return new PageResult<>(total, list);
    }

    public Map<String, Object> getVideoDetails(Long videoId) {
        Video video =  videoDao.getVideoDetails(videoId);
        Long userId = video.getUserId();
        User user = userService.getUserInfo(userId);
        UserInfo userInfo = user.getUserInfo();
        Map<String, Object> result = new HashMap<>();
        result.put("video", video);
        result.put("userInfo", userInfo);
        return result;
    }

//    public void addVideoView(VideoView videoView, HttpServletRequest request) {
//        Long userId = videoView.getUserId();
//        Long videoId = videoView.getVideoId();
//        //生成clientId，先获取useragent再生成clientId
//        String agent = request.getHeader("User-Agent");
//        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
//        String clientId = String.valueOf(userAgent.getId());
//        //获取ip
//        String ip = IpUtil.getIP(request);
//        Map<String, Object> params = new HashMap<>();
//        if(userId != null){
//            //登录
//            params.put("userId", userId);
//        }else{
//            //游客
//            params.put("ip", ip);
//            params.put("clientId", clientId);
//        }
//        Date now = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        params.put("today", sdf.format(now));
//        params.put("videoId", videoId);
//        //添加观看记录
//        VideoView dbVideoView = videoDao.getVideoView(params);
//        if(dbVideoView == null){
//            //如果为空才添加信息，一天只添加一次观看记录
//            videoView.setIp(ip);
//            videoView.setClientId(clientId);
//            videoView.setCreateTime(new Date());
//            videoDao.addVideoView(videoView);
//        }
//    }
//
//    public Integer getVideoViewCounts(Long videoId) {
//        return videoDao.getVideoViewCounts(videoId);
//    }
//
//
//    /**
//     * 基于用户的协同推荐
//     * @param userId 用户id
//     */
//    public List<Video> recommend(Long userId) throws TasteException {
//        List<UserPreference> list = videoDao.getAllUserPreference();
//        //创建数据模型
//        DataModel dataModel = this.createDataModel(list);
//        //获取用户相似程度
//        UserSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
//        System.out.println(similarity.userSimilarity(11, 12));
//        //获取用户邻居
//        UserNeighborhood userNeighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);
//        long[] ar = userNeighborhood.getUserNeighborhood(userId);
//        //构建推荐器
//        Recommender recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);
//        //推荐视频
//        List<RecommendedItem> recommendedItems = recommender.recommend(userId, 5);
//        List<Long> itemIds = recommendedItems.stream().map(RecommendedItem::getItemID).collect(Collectors.toList());
//        return videoDao.batchGetVideosByIds(itemIds);
//    }
//
//    /**
//     * 基于内容的协同推荐
//     * @param userId 用户id
//     * @param itemId 参考内容id（根据该内容进行相似内容推荐）
//     * @param howMany 需要推荐的数量
//     */
//    public List<Video> recommendByItem(Long userId, Long itemId, int howMany) throws TasteException {
//        List<UserPreference> list = videoDao.getAllUserPreference();
//        //创建数据模型
//        DataModel dataModel = this.createDataModel(list);
//        //获取内容相似程度
//        ItemSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
//        GenericItemBasedRecommender genericItemBasedRecommender = new GenericItemBasedRecommender(dataModel, similarity);
//        // 物品推荐相拟度，计算两个物品同时出现的次数，次数越多任务的相拟度越高
//        List<Long> itemIds = genericItemBasedRecommender.recommendedBecause(userId, itemId, howMany)
//                .stream()
//                .map(RecommendedItem::getItemID)
//                .collect(Collectors.toList());
//        //推荐视频
//        return videoDao.batchGetVideosByIds(itemIds);
//    }
//
//    private DataModel createDataModel(List<UserPreference> userPreferenceList) {
//        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
//        Map<Long, List<UserPreference>> map = userPreferenceList.stream().collect(Collectors.groupingBy(UserPreference::getUserId));
//        Collection<List<UserPreference>> list = map.values();
//        for(List<UserPreference> userPreferences : list){
//            GenericPreference[] array = new GenericPreference[userPreferences.size()];
//            for(int i = 0; i < userPreferences.size(); i++){
//                UserPreference userPreference = userPreferences.get(i);
//                GenericPreference item = new GenericPreference(userPreference.getUserId(), userPreference.getVideoId(), userPreference.getValue());
//                array[i] = item;
//            }
//            fastByIdMap.put(array[0].getUserID(), new GenericUserPreferenceArray(Arrays.asList(array)));
//        }
//        return new GenericDataModel(fastByIdMap);
//    }
//
////    public List<VideoBinaryPicture> convertVideoToImage(Long videoId, String fileMd5) throws Exception{
////        com.mybilibili.domain.File file = fileService.getFileByMd5(fileMd5);
////        String filePath = "/Users/hat/tmpfile/fileForVideoId" + videoId + "." + file.getType();
////        fastDFSUtil.downLoadFile(file.getUrl(), filePath);
////        FFmpegFrameGrabber fFmpegFrameGrabber = FFmpegFrameGrabber.createDefault(filePath);
////        fFmpegFrameGrabber.start();
////        int ffLength = fFmpegFrameGrabber.getLengthInFrames();
////        Frame frame;
////        Java2DFrameConverter converter = new Java2DFrameConverter();
////        int count = 1;
////        List<VideoBinaryPicture> pictures = new ArrayList<>();
////        for(int i=1; i<= ffLength; i ++){
////            long timestamp = fFmpegFrameGrabber.getTimestamp();
////            frame = fFmpegFrameGrabber.grabImage();
////            if(count == i){
////                if(frame == null){
////                    throw new ConditionException("无效帧");
////                }
////                BufferedImage bufferedImage = converter.getBufferedImage(frame);
////                ByteArrayOutputStream os = new ByteArrayOutputStream();
////                ImageIO.write(bufferedImage, "png", os);
////                InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
////                //输出黑白剪影文件
////                java.io.File outputFile = java.io.File.createTempFile("convert-" + videoId + "-", ".png");
////                BufferedImage binaryImg = imageUtil.getBodyOutline(bufferedImage, inputStream);
////                ImageIO.write(binaryImg, "png", outputFile);
////                //有的浏览器或网站需要把图片白色的部分转为透明色，使用以下方法可实现
////                imageUtil.transferAlpha(outputFile, outputFile);
////                //上传视频剪影文件
////                String imgUrl = fastDFSUtil.uploadCommonFile(outputFile, "png");
////                VideoBinaryPicture videoBinaryPicture = new VideoBinaryPicture();
////                videoBinaryPicture.setFrameNo(i);
////                videoBinaryPicture.setUrl(imgUrl);
////                videoBinaryPicture.setVideoId(videoId);
////                videoBinaryPicture.setVideoTimestamp(timestamp);
////                pictures.add(videoBinaryPicture);
////                count += FRAME_NO;
////                //删除临时文件
////                outputFile.delete();
////            }
////        }
////        //删除临时文件
////        File tmpFile = new File(filePath);
////        tmpFile.delete();
////        //批量添加视频剪影文件
////        videoDao.batchAddVideoBinaryPictures(pictures);
////        return pictures;
////    }
//
//    public List<VideoTag> getVideoTagsByVideoId(Long videoId) {
//        return videoDao.getVideoTagsByVideoId(videoId);
//    }
//
//    public void deleteVideoTags(List<Long> tagIdList, Long videoId) {
//        videoDao.deleteVideoTags(tagIdList, videoId);
//    }
//
////    public List<VideoBinaryPicture> getVideoBinaryImages(Map<String, Object> params) {
////        return videoDao.getVideoBinaryImages(params);
////    }

}