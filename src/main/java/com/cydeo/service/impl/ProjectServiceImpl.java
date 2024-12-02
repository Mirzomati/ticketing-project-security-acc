package com.cydeo.service.impl;

import com.cydeo.dto.ProjectDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.Project;
import com.cydeo.entity.User;
import com.cydeo.enums.Status;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.repository.ProjectRepository;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final MapperUtil mapperUtil;
    private final UserService userService;
    private final TaskService taskService;

    public ProjectServiceImpl(ProjectRepository projectRepository, MapperUtil mapperUtil, UserService userService, TaskService taskService) {
        this.projectRepository = projectRepository;
        this.mapperUtil = mapperUtil;
        this.userService = userService;
        this.taskService = taskService;
    }


    @Override
    public ProjectDTO getByProjectCode(String projectCode) {

        Project project = projectRepository.findByProjectCode(projectCode);

        return mapperUtil.convert(project, ProjectDTO.class);
    }

    @Override
    public List<ProjectDTO> listAllProjects() {

        List<Project> list = projectRepository.findAll(Sort.by("projectCode"));

        return list.stream().map(project -> mapperUtil.convert(project, ProjectDTO.class)).collect(Collectors.toList());
    }

    @Override
    public void save(ProjectDTO project) {

        project.setProjectStatus(Status.OPEN);
        Project convertedProject = mapperUtil.convert(project, Project.class);
        projectRepository.save(convertedProject);

    }

    @Override
    public void update(ProjectDTO project) {

        Project foundProject = projectRepository.findByProjectCode(project.getProjectCode());

        Project convertedProject = mapperUtil.convert(project, Project.class);

        convertedProject.setId(foundProject.getId());

        convertedProject.setProjectStatus(foundProject.getProjectStatus());

        projectRepository.save(convertedProject);

    }

    @Override
    public void delete(String projectCode) {

        Project project = projectRepository.findByProjectCode(projectCode);

        project.setIsDeleted(true);
        project.setProjectCode(project.getProjectCode() + "-" + project.getId()); // SP00-1

        projectRepository.save(project);

        taskService.deleteByProject(mapperUtil.convert(project, ProjectDTO.class));
    }

    @Override
    public void complete(String projectCode) {

        Project project = projectRepository.findByProjectCode(projectCode);

        project.setProjectStatus(Status.COMPLETE);

        projectRepository.save(project);

        taskService.completeByProject(mapperUtil.convert(project, ProjectDTO.class));

    }

    @Override
    public List<ProjectDTO> listAllProjectDetails() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        UserDTO currentUser = userService.findByUserName(username);

        User user = mapperUtil.convert(currentUser, User.class);

        List<Project> list = projectRepository.findAllByAssignedManager(user);

       return list.stream().map(project -> {

            ProjectDTO dto = mapperUtil.convert(project, ProjectDTO.class);

            dto.setUnfinishedTaskCounts(taskService.totalNonCompletedTask(project.getProjectCode()));
            dto.setCompleteTaskCounts(taskService.totalCompletedTask(project.getProjectCode()));

            return dto;

        }).collect(Collectors.toList());

    }

    @Override
    public List<ProjectDTO> listAllNonCompletedByAssignedManager(UserDTO assignedManager) {

        List<Project> projects = projectRepository.findAllByProjectStatusIsNotAndAssignedManager(Status.COMPLETE, mapperUtil.convert(assignedManager, User.class));

        return projects.stream().map(project -> mapperUtil.convert(project, ProjectDTO.class)).collect(Collectors.toList());
    }
}
