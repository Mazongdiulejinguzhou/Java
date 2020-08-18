package cn.com.codingce.config;

import cn.com.codingce.pojo.User;
import cn.com.codingce.service.UserServiceImpl;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

//自定义的Realm
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private UserServiceImpl userService;

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("执行了授权");
        return null;
    }

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("执行了认证");
        //用户名，密码
        String name = "root";
        String password = "123";

         UsernamePasswordToken userToken= (UsernamePasswordToken)authenticationToken;

         //连接真实数据库
        User user = userService.queryUserByName(userToken.getUsername());
        System.out.println(user.toString());
        if (user == null) {
            return null;
        }

//        if (!userToken.getUsername().equals(name)) {
//             return null;   //UnknownAccountException
//         }

         //密码认证
//        return new SimpleAuthenticationInfo("", password, "");

        //连数据库
        return new SimpleAuthenticationInfo("", user.getPwd(), "");

    }
}
