package org.kickmyb.server;

import org.h2.util.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kickmyb.server.account.BadCredentialsException;
import org.kickmyb.server.account.MUser;
import org.kickmyb.server.account.MUserRepository;
import org.kickmyb.server.account.ServiceAccount;
import org.kickmyb.server.task.MTask;
import org.kickmyb.server.task.ServiceTask;
import org.kickmyb.transfer.AddTaskRequest;
import org.kickmyb.transfer.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO pour celui ci on aimerait pouvoir mocker l'utilisateur pour ne pas avoir à le créer

// https://reflectoring.io/spring-boot-mock/#:~:text=This%20is%20easily%20done%20by,our%20controller%20can%20use%20it.

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = KickMyBServerApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@ActiveProfiles("test")
class ServiceTaskTests {

    @Autowired
    private MUserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServiceTask serviceTask;

    @Autowired
    private ServiceAccount serviceAccount;

    @Test
    void testAjouterTacheOk() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing,
            ServiceAccount.UsernameTooShort, ServiceAccount.PasswordTooShort,
            ServiceAccount.UsernameAlreadyTaken, BadCredentialsException {

        // on crée un compte
        SignupRequest req = new SignupRequest();
        req.username = "alice";
        req.password = "Passw0rd!";
        serviceAccount.signup(req);

        // on récupère l'utilisateur
        MUser alice = serviceTask.userFromUsername("alice");

        // on crée une tâche
        AddTaskRequest addTaskRequest = new AddTaskRequest();
        addTaskRequest.name = "Tâche 1";
        addTaskRequest.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        // on ajoute la tâche à l'utilisateur
        serviceTask.addOne(addTaskRequest, alice);

        // on vérifie que la tâche a bien été ajoutée
        assertEquals(1, serviceTask.home(alice.id).size());
    }

    @Test
    void testAddTaskEmpty()  {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.Empty");
        } catch (Exception e) {
            assertEquals(ServiceTask.Empty.class, e.getClass());
        }
    }

    @Test
    void testAddTaskTooShort() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "o";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.TooShort");
        } catch (Exception e) {
            assertEquals(ServiceTask.TooShort.class, e.getClass());
        }
    }

    @Test
    void testAddTaskExisting() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Bonne tâche";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.Existing");
        } catch (Exception e) {
            assertEquals(ServiceTask.Existing.class, e.getClass());
        }
    }

    @Test
    void testDeleteExistingTaskOK() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing{
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Tâche de test";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        serviceTask.addOne(atr, u);
        List<MTask> listTache = u.tasks;

        MTask tacheASupp = listTache.get(0);
        Long taskId = tacheASupp.id;
        // ici la tache est dans la BD mais pas d ans la copie en RAM de u user
        u = serviceTask.userFromUsername(u.username); // u a maintenant une tache dans sa liste
        serviceTask.deleteTask(taskId, u);

        List<MTask> updatedTasks = u.tasks;
        assertTrue(updatedTasks.isEmpty(), "Task should be deleted");
    }

    @Test
    void testDeleteNonExistingTask(){
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);
        int id = 0;
        Long longId = (long) id;
        try{
            serviceTask.deleteTask(longId, u);
            fail("Aurait du lancer Runtime Exception");
        } catch (Exception e) {
            assertEquals(RuntimeException.class, e.getClass());
        }
    }

    @Test
    void testDeleteOtherUsersTask() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing{
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        MUser u2 = new MUser();
        u.username = "M. Voleur";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Tâche de test";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        serviceTask.addOne(atr, u);
        List<MTask> listTache = u.tasks;

        MTask tacheASupp = listTache.get(0);
        Long taskId = tacheASupp.id;
        try{
            serviceTask.deleteTask(taskId, u2);
            fail("Aurait du lancer Runtime Exception");
        } catch (Exception e) {
            assertEquals(RuntimeException.class, e.getClass());
        }
    }
}
