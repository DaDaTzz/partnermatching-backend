package com.da.usercenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.dto.team.*;
import com.da.usercenter.model.entity.Post;
import com.da.usercenter.model.entity.Team;
import com.da.usercenter.model.vo.PostVO;
import com.da.usercenter.model.vo.TeamUserVO;
import com.da.usercenter.service.TeamService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 队伍接口
 *
 * @author 达
 */
@RestController
@RequestMapping("/team")
//@CrossOrigin(origins = {"http://8.130.133.165"},allowCredentials = "true")
//@CrossOrigin(origins = {"http://127.0.0.1:5173"},allowCredentials = "true")
public class TeamController {

    @Resource
    private TeamService teamService;


    @PostMapping("/add")
    public ResponseResult<Long> createTeam(@RequestBody CreateTeamRequest createTeamRequest, HttpServletRequest request) {
        Long teamId = teamService.createTeam(createTeamRequest, request);
        return ResponseResult.success(teamId);
    }


    @PostMapping("/update")
    public ResponseResult<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        Boolean res = teamService.updateTeam(teamUpdateRequest, request);
        return ResponseResult.success(res);
    }

    @GetMapping("/query")
    public ResponseResult<Team> getTeamById(long id) {
        System.out.println(id);
        Team team = teamService.getTeamById(id);
        return ResponseResult.success(team);
    }

    @GetMapping("/list")
    public ResponseResult<List<TeamUserVO>> getTeamList(TeamQuery teamQuery, HttpServletRequest request) {
        List<TeamUserVO> teamUserVOList = teamService.getTeamList(teamQuery, request);
        return ResponseResult.success(teamUserVOList);
    }

    @GetMapping("/list/page")
    public ResponseResult<Page<Team>> getTeamListByPage(TeamQuery teamQuery) {
        Page<Team> teamPage = teamService.getTeamListByPage(teamQuery);
        return ResponseResult.success(teamPage);
    }

    @PostMapping("/join")
    public ResponseResult<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        Boolean res = teamService.joinTeam(teamJoinRequest, request);
        return ResponseResult.success(res);
    }

    @PostMapping("/exit")
    public ResponseResult<Boolean> exitTeam(@RequestBody TeamExitRequest teamExitRequest, HttpServletRequest request) {
        Boolean res = teamService.exitTeam(teamExitRequest, request);
        return ResponseResult.success(res);
    }


    @PostMapping("/disband")
    public ResponseResult<Boolean> disbandTeam(@RequestBody TeamDisband teamDisband, HttpServletRequest request) {
        Boolean res = teamService.disbandTeam(teamDisband, request);
        return ResponseResult.success(res);
    }

    @GetMapping("/list/create")
    public ResponseResult<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request){
        List<TeamUserVO> teamUserVOList = teamService.listMyCreateTeams(teamQuery, request);
        return ResponseResult.success(teamUserVOList);
    }

    @GetMapping("/list/join")
    public ResponseResult<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request){
        List<TeamUserVO> teamUserVOList = teamService.listMyJoinTeams(teamQuery, request);
        return ResponseResult.success(teamUserVOList);
    }

    /**
     * 根据 id 获取队伍信息
     */
    @GetMapping("/get/vo")
    public ResponseResult<TeamUserVO> getTeamUserVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResponseResult.success(teamService.getTeamUserVO(team, request));
    }




}
