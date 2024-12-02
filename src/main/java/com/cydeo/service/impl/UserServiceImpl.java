package com.cydeo.service.impl;

import com.cydeo.dto.ProjectDTO;
import com.cydeo.dto.TaskDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.User;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MapperUtil mapperUtil;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, MapperUtil mapperUtil, @Lazy ProjectService projectService, @Lazy TaskService taskService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mapperUtil = mapperUtil;
        this.projectService = projectService;
        this.taskService = taskService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDTO> listAllUsers() {

        List<User> userList = userRepository.findAllByIsDeletedOrderByFirstNameDesc(false);

        return userList.stream().map(user -> mapperUtil.convert(user, UserDTO.class)).collect(Collectors.toList());
    }

    @Override
    public UserDTO findByUserName(String username) {

        User user = userRepository.findByUserNameAndIsDeleted(username, false);

        return mapperUtil.convert(user, UserDTO.class);
    }

    @Override
    public void save(UserDTO user) {

        User convertedUser = mapperUtil.convert(user, User.class);

        convertedUser.setPassWord(passwordEncoder.encode(user.getPassWord()));

        userRepository.save(convertedUser);
    }

    @Override
    public void update(UserDTO user) {

        User foundUser = userRepository.findByUserNameAndIsDeleted(user.getUserName(), false);

        User updatedUser = mapperUtil.convert(user, User.class);

        updatedUser.setId(foundUser.getId());

        userRepository.save(updatedUser);
    }

    @Override
    public void delete(String username) {

        User user = userRepository.findByUserNameAndIsDeleted(username, false);


        if (checkIfUserCanBeDeleted(mapperUtil.convert(user, UserDTO.class))){

            user.setIsDeleted(true);
            user.setUserName(user.getUserName() + "-" + user.getId());

            userRepository.save(user);
        }
    }

    private boolean checkIfUserCanBeDeleted(UserDTO user){

        switch (user.getRole().getDescription()){

            case "Manager":

                List<ProjectDTO> projectDTOList = projectService.listAllNonCompletedByAssignedManager(user);

                return projectDTOList.size() == 0;

            case "Employee":

                List<TaskDTO> taskDTOList = taskService.listAllNonCompletedByAssignedEmployee(user);

                return taskDTOList.size() == 0;

            default:

                return true;

        }



    }



    @Override
    public List<UserDTO> listAllByRole(String role) {

        List<User> users = userRepository.findByRoleDescriptionIgnoreCaseAndIsDeleted(role, false);

        return users.stream().map(user -> mapperUtil.convert(user, UserDTO.class)).collect(Collectors.toList());
    }
}
