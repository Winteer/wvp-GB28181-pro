package com.genersoft.iot.vmp.storager.dao;

import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;
import com.genersoft.iot.vmp.vmanager.gb28181.platform.bean.ChannelReduce;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 用于存储设备通道信息
 */
@Mapper
@Repository
public interface DeviceChannelMapper {

    @Insert("INSERT INTO device_channel (channelId, deviceId, name, manufacture, model, owner, civilCode, block, " +
            "address, parental, parentId, safetyWay, registerWay, certNum, certifiable, errCode, secrecy, " +
            "ipAddress, port, password, PTZType, status, streamId, longitude, latitude, createTime, updateTime) " +
            "VALUES ('${channelId}', '${deviceId}', '${name}', '${manufacture}', '${model}', '${owner}', '${civilCode}', '${block}'," +
            "'${address}', ${parental}, '${parentId}', ${safetyWay}, ${registerWay}, '${certNum}', ${certifiable}, ${errCode}, '${secrecy}', " +
            "'${ipAddress}', ${port}, '${password}', ${PTZType}, ${status}, '${streamId}', ${longitude}, ${latitude},'${createTime}', '${updateTime}')")
    int add(DeviceChannel channel);

    @Update(value = {" <script>" +
            "UPDATE device_channel " +
            "SET updateTime='${updateTime}'" +
            "<if test=\"name != null\">, name='${name}'</if>" +
            "<if test=\"manufacture != null\">, manufacture='${manufacture}'</if>" +
            "<if test=\"model != null\">, model='${model}'</if>" +
            "<if test=\"owner != null\">, owner='${owner}'</if>" +
            "<if test=\"civilCode != null\">, civilCode='${civilCode}'</if>" +
            "<if test=\"block != null\">, block='${block}'</if>" +
            "<if test=\"address != null\">, address='${address}'</if>" +
            "<if test=\"parental != null\">, parental=${parental}</if>" +
            "<if test=\"parentId != null\">, parentId='${parentId}'</if>" +
            "<if test=\"safetyWay != null\">, safetyWay=${safetyWay}</if>" +
            "<if test=\"registerWay != null\">, registerWay=${registerWay}</if>" +
            "<if test=\"certNum != null\">, certNum='${certNum}'</if>" +
            "<if test=\"certifiable != null\">, certifiable=${certifiable}</if>" +
            "<if test=\"errCode != null\">, errCode=${errCode}</if>" +
            "<if test=\"secrecy != null\">, secrecy='${secrecy}'</if>" +
            "<if test=\"ipAddress != null\">, ipAddress='${ipAddress}'</if>" +
            "<if test=\"port != null\">, port=${port}</if>" +
            "<if test=\"password != null\">, password='${password}'</if>" +
            "<if test=\"PTZType != null\">, PTZType=${PTZType}</if>" +
            "<if test=\"status != null\">, status='${status}'</if>" +
            "<if test=\"streamId != null\">, streamId='${streamId}'</if>" +
            "<if test=\"hasAudio != null\">, hasAudio=${hasAudio}</if>" +
            "<if test=\"longitude != null\">, longitude=${longitude}</if>" +
            "<if test=\"latitude != null\">, latitude=${latitude}</if>" +
            "WHERE deviceId='${deviceId}' AND channelId='${channelId}'"+
            " </script>"})
    int update(DeviceChannel channel);

    @Select(value = {" <script>" +
            " SELECT * FROM device_channel  " +
            " WHERE 1=1  " +
            " <if test=\"hasSubChannel == true\" > AND channelId IN ( SELECT DISTINCT ( parentId ) FROM device_channel ) </if>" +
            " <if test=\"hasSubChannel == false\" > AND channelId NOT IN ( SELECT DISTINCT ( parentId ) FROM device_channel ) </if>" +
            " AND deviceId=#{deviceId} " +
            " <if test=\"query != null\"> AND (channelId LIKE '%${query}%' OR name LIKE '%${query}%' OR name LIKE '%${query}%')</if> " +
            " <if test=\"parentChannelId != null\"> AND parentId=#{parentChannelId} </if> " +
            " <if test=\"online == true\" > AND status=1</if>" +
            " <if test=\"online == false\" > AND status=0</if>" +
            " ORDER BY channelId ASC" +
            " </script>"})
    List<DeviceChannel> queryChannelsByDeviceId(String deviceId, String parentChannelId, String query, Boolean hasSubChannel, Boolean online);

    /**
     * 根据设备id查询子目录下数量
     * @param deviceId
     * @return
     */
    @Select(value = {" <script>" +
            " SELECT zml.channelId , (SELECT count(0) FROM device_channel WHERE parentId=zml.channelId) as subCount FROM  " +
            " ( SELECT channelId FROM device_channel WHERE  channelId IN ( SELECT DISTINCT ( parentId ) FROM device_channel WHERE deviceId = #{deviceId} ) ) zml  " +
            " </script>"})
    List<Map<String,Integer>> queryChannelsCountByDeviceId(String deviceId);

    @Select(value = {" <script>" +
            " SELECT DISTINCT(SELECT count(0) FROM device_channel WHERE parentId=zml.channelId) as subCount FROM  " +
            " ( SELECT channelId FROM device_channel WHERE  channelId = #{channelId} ) zml  " +
            " </script>"})
    Integer countSubCount(String channelId);

    @Select("SELECT * FROM device_channel WHERE deviceId=#{deviceId} AND channelId=#{channelId}")
    DeviceChannel queryChannel(String deviceId, String channelId);

    @Delete("DELETE FROM device_channel WHERE deviceId=#{deviceId}")
    int cleanChannelsByDeviceId(String deviceId);

    @Update(value = {"UPDATE device_channel SET streamId=null WHERE deviceId=#{deviceId} AND channelId=#{channelId}"})
    void stopPlay(String deviceId, String channelId);

    @Update(value = {"UPDATE device_channel SET streamId=#{streamId} WHERE deviceId=#{deviceId} AND channelId=#{channelId}"})
    void startPlay(String deviceId, String channelId, String streamId);


    @Select(value = {" <script>" +
            "SELECT * FROM ( "+
                " SELECT dc.channelId, dc.deviceId, dc.name, de.manufacturer, de.hostAddress, " +
                "(SELECT count(0) FROM device_channel WHERE parentId=dc.channelId) as subCount, " +
                "(SELECT pc.platformId FROM platform_gb_channel pc WHERE pc.deviceId=dc.deviceId AND pc.channelId = dc.channelId ) as platformId " +
                "FROM device_channel dc " +
                "LEFT JOIN device de ON dc.deviceId = de.deviceId " +
                " WHERE 1=1 " +
                " <if test=\"query != null\"> AND (dc.channelId LIKE '%${query}%' OR dc.name LIKE '%${query}%' OR dc.name LIKE '%${query}%')</if> " +
                " <if test=\"online == true\" > AND dc.status=1</if> " +
                " <if test=\"online == false\" > AND dc.status=0</if> " +
            ") dcr" +
            " WHERE 1=1 " +
            " <if test=\"hasSubChannel!= null and hasSubChannel == true\" >  AND subCount >0</if> " +
            " <if test=\"hasSubChannel!= null and hasSubChannel == false\" >  AND subCount=0</if> " +
            " <if test=\"platformId != null and inPlatform == true \" >  AND platformId='${platformId}'</if> " +
            " <if test=\"platformId != null and inPlatform == false \" >  AND (platformId != '${platformId}' OR platformId is NULL )  </if> " +
            " ORDER BY deviceId, channelId ASC" +
            " </script>"})

    List<ChannelReduce> queryChannelListInAll(String query, Boolean online, Boolean hasSubChannel, String platformId, Boolean inPlatform);
}
