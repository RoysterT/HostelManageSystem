package com.hostelms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hostelms.entity.dto.Identity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IdentityService extends IService<Identity> {
    Identity getIdentityById(int id);

    Identity getIdentityByName(String name);

    List<Identity> getIdentityList();

    String removeIdentityById(int id);

    String updateIdentityById(Identity identity);

    String addIdentity(Identity identity);
}
