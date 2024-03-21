package com.kiwidev.junit.service;

import com.kiwidev.junit.dto.User;
import com.kiwidev.junit.paramresolver.UserServiceParamResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@Tag("fast")
@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith({
        UserServiceParamResolver.class
})
public class UserServiceTest {

    private static final User IVAN = User.of(1,"Ivan","123");
    private static final User PETR = User.of(2,"Petr","111");

    private UserService userService;

    UserServiceTest(TestInfo testInfo){
        System.out.println(testInfo);
    }


    @BeforeAll
    void init(){
        System.out.println("Before all:" + this);
    }

    @BeforeEach
    void prepare(UserService userService){
        System.out.println("Before each: " + this);
        this.userService = userService;
    }

    @Test
    void usersEmptyIfNoUserAdded(UserService userService){
        System.out.println("Test1: " + this);
        var users = userService.getAll();
        assertTrue(users.isEmpty());

    }

    @Test
    void usersSizeIfUserAdded(){
        var userService = new UserService();
        userService.add(IVAN);
        userService.add(PETR);

        var users = userService.getAll();
        assertThat(users).hasSize(2);
//        assertEquals(2,users.size());
    }




    @Test

    void usersConvertedToMapById(){
        userService.add(IVAN,PETR);

        Map<Integer,User> users = userService.getAllConvertedById();

        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(),PETR.getId()),
                () -> assertThat(users).containsValues(IVAN,PETR)
        );

    }




    @Test
    void throwExceptionIfUsernameOrPasswordIsNull(){
        assertAll(
            () ->assertThrows(IllegalArgumentException.class,() -> userService.login(null,"dummy")),
            () ->assertThrows(IllegalArgumentException.class,() -> userService.login("dummy",null))
        );
    }

    @AfterEach
    void deleteDataFromDatabase(){
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool(){
        System.out.println("After all: " + this);
    }


    @Tag("login")
    @DisplayName("user login test functionality")
    @Nested
    class LoginTest{
        @Test

        void loginFailedIfUsernameIsNotCorrect(){
            userService.add(IVAN);

            Optional<User> user = userService.login("dummy",IVAN.getPassword());

            assertTrue(user.isEmpty());
        }
        @Test

        void loginFailedIfPasswordIsNotCorrect(){
            userService.add(IVAN);

            Optional<User> user = userService.login(IVAN.getUsername(),"dummy");

            assertTrue(user.isEmpty());
        }
        @Test

        void loginSuccessIfUserExists(){
            userService.add(IVAN);

            Optional<User> maybeuser = userService.login(IVAN.getUsername(),IVAN.getPassword());
            assertThat(maybeuser).isPresent();
            maybeuser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));


//        assertTrue(maybeuser.isPresent());
//        maybeuser.ifPresent(user -> assertEquals(IVAN,user));
        }

        @Test
        void loginFailedIfUserDoesNotExists(){
            userService.add(IVAN);

            var user = userService.login("dummy",IVAN.getPassword());

            assertTrue(user.isEmpty());
        }
        @ParameterizedTest(name = "{arguments} test")
//    @ArgumentsSource()
//        @NullSource
//        @EmptySource
////    @NullAndEmptySource
//        @ValueSource(strings = {
//                "Ivan","Petr"
//        })
//    @EnumSource
        @MethodSource("com.kiwidev.junit.service.UserServiceTest#getArgumentsForLoginTest")
//        @CsvFileSource(resources = "/login-test-data.csv",delimiter = ',',numLinesToSkip = 1)
        @DisplayName("login param test")
        void loginParametrizedTest(String username,String password, Optional<User> user){
            userService.add(IVAN,PETR);
            Optional<User> maybeUser = userService.login(username,password);
            assertThat(maybeUser).isEqualTo(user);
        }


    }
    static Stream<Arguments> getArgumentsForLoginTest(){
        return Stream.of(
                Arguments.of("Ivan","123",Optional.of(IVAN)),
                Arguments.of("Petr","111",Optional.of(PETR)),
                Arguments.of("Petr","dummy",Optional.empty()),
                Arguments.of("dummy","123",Optional.empty())

        );
    }


}
