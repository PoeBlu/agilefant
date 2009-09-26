package fi.hut.soberit.agilefant.business;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import fi.hut.soberit.agilefant.business.impl.UserBusinessImpl;
import fi.hut.soberit.agilefant.db.UserDAO;
import fi.hut.soberit.agilefant.model.Holiday;
import fi.hut.soberit.agilefant.model.User;

public class UserBusinessTest {

    UserBusinessImpl userBusiness = new UserBusinessImpl();
    UserDAO userDAO;
    
    @Before
    public void setUp() {
        userDAO = createMock(UserDAO.class);
        userBusiness.setUserDAO(userDAO);
    }

    private void verifyAll() {
        verify(userDAO);
    }

    private void replayAll() {
        replay(userDAO);
    }
    
    @Test
    public void testGetEnabledUsers_interaction() {
        List<User> listOfEnabledUsers = Arrays.asList(new User());
        expect(userDAO.listUsersByEnabledStatus(true)).andReturn(listOfEnabledUsers);
        replayAll();
        
        assertSame(listOfEnabledUsers, userBusiness.getEnabledUsers());
        
        verifyAll();
    }

    
    @Test
    public void testGetDisabledUsers_interaction() {
        List<User> listOfDisabledUsers = Arrays.asList(new User());
        expect(userDAO.listUsersByEnabledStatus(false)).andReturn(listOfDisabledUsers);
        replayAll();
        
        assertSame(listOfDisabledUsers, userBusiness.getDisabledUsers());
        
        verifyAll();
    }
    
    @Test
    public void testCalculateWorktimePerPeriod() {
        User user = new User();
        LocalDate start = new LocalDate(2009,6,1);
        Duration expected = new Duration(start.toDateMidnight(), start.plusDays(4).toDateMidnight());
        Interval interval = new Interval(start.toDateMidnight(), start.plusDays(4).toDateMidnight());
        Duration actual = this.userBusiness.calculateWorktimePerPeriod(user, interval);
        assertEquals(expected.getMillis(), actual.getMillis());
    }
    
    @Test
    public void testCalculateWorktimePerPeriod_inWeekend() {
        User user = new User();
        DateTime start = new DateTime(2009,9,5, 14, 50, 0, 0);
        Duration expected = new Duration(0);
        Interval interval = new Interval(start, start.plusDays(1).toDateMidnight());
        Duration actual = this.userBusiness.calculateWorktimePerPeriod(user, interval);
        assertEquals(expected.getMillis(), actual.getMillis());
    }
    
    @Test
    public void testCalculateWorktimePerPeriod_withWeekend() {
        User user = new User();
        LocalDate start = new LocalDate(2009,6,1);
        Duration expected = new Duration(start.toDateMidnight(), start.plusDays(6).toDateMidnight());
        Interval interval = new Interval(start.toDateMidnight(), start.plusDays(8).toDateMidnight());
        Duration actual = this.userBusiness.calculateWorktimePerPeriod(user, interval);
        assertEquals(expected.getMillis(), actual.getMillis());
    }
    
    @Test
    public void testCalculateWorktimePerPeriod_withVacations() {
        User user = new User();
        LocalDate start = new LocalDate(2009,6,1);
        
        Holiday holiday = new Holiday();
        holiday.setStartDate(start.plusDays(1).toDateMidnight().toDate());
        holiday.setEndDate(start.plusDays(3).toDateMidnight().toDate());
        user.getHolidays().add(holiday);        
        
        Duration expected = new Duration(start.toDateMidnight(), start.plusDays(4).toDateMidnight());
        Interval interval = new Interval(start.toDateMidnight(), start.plusDays(8).toDateMidnight());
        Duration actual = this.userBusiness.calculateWorktimePerPeriod(user, interval);
        assertEquals(expected.getMillis(), actual.getMillis());        
    }
    
    @Test
    public void testStore_newUser() {
        User user = new User();
        user.setFullName("Teemu Teekkari");
        
        expect(userDAO.create(user)).andReturn(1756);
        expect(userDAO.get(1756)).andReturn(user);
        replayAll();
        User actual = userBusiness.storeUser(user, "teemu");
        verifyAll();
        
        assertEquals("Teemu Teekkari", actual.getFullName());
    }
    
    @Test
    public void testStore_noPasswordChange() {
        User dataItem = new User();
        dataItem.setId(123);
        dataItem.setPassword("password string");
        
        userDAO.store(dataItem);
        replayAll();
        User actual = userBusiness.storeUser(dataItem, null);
        verifyAll();
        
        assertEquals("password string", actual.getPassword());
    }
    
    @Test
    public void testStore_passwordChange() {
        String password = "teemu";
        String md5hash = "f38bb5caf4771ef31e2d8456e5e93f2f";
        
        User user = new User();
        user.setId(123);
        user.setPassword("Foo");
        
        userDAO.store(user);
        replayAll();
        User actual = userBusiness.storeUser(user, password);
        verifyAll();
        
        assertEquals(md5hash, actual.getPassword());
    }
    
    @Test
    public void testRetrieveMultiple() {
        Set<Integer> userIds = new HashSet<Integer>(Arrays.asList(1,2));
        expect(userDAO.get(1)).andReturn(new User());
        expect(userDAO.get(2)).andReturn(new User());
        replayAll();
        Collection<User> actual = userBusiness.retrieveMultiple(userIds);
        verifyAll();
        assertEquals(2, actual.size());
    }
}
