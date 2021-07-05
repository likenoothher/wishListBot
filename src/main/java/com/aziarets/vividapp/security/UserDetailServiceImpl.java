package com.aziarets.vividapp.security;

import com.aziarets.vividapp.dao.BotUserDaoImpl;
import com.aziarets.vividapp.model.BotUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

@Component
@Transactional
public class UserDetailServiceImpl implements UserDetailsService {

    private final BotUserDaoImpl botUserDao;

    @Autowired
    public UserDetailServiceImpl(BotUserDaoImpl botUserDao) {
        this.botUserDao = botUserDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BotUser botUser = botUserDao.getByUserName(username);
        if(botUser != null) {
            return new UserDetails() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(botUser.getBotUserRole());
                }

                @Override
                public String getPassword() {
                    return botUser.getPassword();
                }

                @Override
                public String getUsername() {
                    return botUser.getUserName();
                }

                @Override
                public boolean isAccountNonExpired() {
                    return true;
                }

                @Override
                public boolean isAccountNonLocked() {
                    return true;
                }

                @Override
                public boolean isCredentialsNonExpired() {
                    return true;
                }

                @Override
                public boolean isEnabled() {
                    return true;
                }
            };
        }
        return null;
        //TO DO: remake whole stuff
    }
}
