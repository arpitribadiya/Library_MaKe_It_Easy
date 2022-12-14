package com.CanadaEats.group13.authentication.repository;

import com.CanadaEats.group13.authentication.dto.UserDetailsDto;
import com.CanadaEats.group13.authentication.dto.UserLoginDto;
import com.CanadaEats.group13.authentication.model.response.UserDetailsResponseModel;
import com.CanadaEats.group13.authentication.model.response.UserLoginResponseModel;
import com.CanadaEats.group13.database.IDatabaseConnection;
import com.CanadaEats.group13.utils.ApplicationConstants;
import com.CanadaEats.group13.utils.PasswordEncoderDecoder;
import org.springframework.beans.BeanUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserRepository implements IUserRepository {

    IDatabaseConnection databaseConnection;
    // DatabaseConnection databaseConnection;
    Connection connection;
    Statement statement;
    ResultSet userResult;

    public UserRepository(IDatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public UserDetailsResponseModel registerUser(UserDetailsDto userDetails) {
        UserDetailsResponseModel userResponse = new UserDetailsResponseModel();
        try {
            // databaseConnection = DatabaseConnection.getInstance();
            connection = databaseConnection.getDatabaseConnection();
            statement = connection.createStatement();
            String getUser = "select * from User where UserName = '" + userDetails.getUserName() + "' and Status = 1";
            userResult = statement.executeQuery(getUser);

            if (userResult.next() == false) {
                String insertUser = "insert into User (UserId, FirstName, LastName, EmailId, UserName, Password, MobileNumber, Gender, BirthDate, Address, City, Province, Country, PostalCode, Status, Role_RoleId) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement prepStat = connection.prepareStatement(insertUser);
                prepStat.setString(1, userDetails.getUserId());
                prepStat.setString(2, userDetails.getFirstName());
                prepStat.setString(3, userDetails.getLastName());
                prepStat.setString(4, userDetails.getEmailId());
                prepStat.setString(5, userDetails.getUserName());
                prepStat.setString(6, userDetails.getPassword());
                prepStat.setString(7, userDetails.getMobileNumber());
                prepStat.setString(8, userDetails.getGender());
                prepStat.setDate(9, userDetails.getBirthDate());
                prepStat.setString(10, userDetails.getAddress());
                prepStat.setString(11, userDetails.getCity());
                prepStat.setString(12, userDetails.getProvince());
                prepStat.setString(13, userDetails.getCountry());
                prepStat.setString(14, userDetails.getPostalCode());
                prepStat.setInt(15, userDetails.getStatus());
                prepStat.setString(16, userDetails.getRoleId());
                int result = prepStat.executeUpdate();

                if (result > 0) {
                    BeanUtils.copyProperties(userDetails, userResponse);
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception : UserRepository - registerUser()");
            System.out.println(ex);
            System.out.println(ex.getMessage());
        } finally {
            try {
                connection.close();
                statement.close();
                userResult.close();
            } catch (Exception ex) {
                System.out.println("Exception : UserRepository - Closing database connection in registerUser()");
            }
        }

        return userResponse;
    }

    public UserLoginResponseModel loginUser(UserLoginDto userLoginDto) {
        UserLoginResponseModel userLoginResponseModel = new UserLoginResponseModel();

        try {
            // databaseConnection = DatabaseConnection.getInstance();
            connection = databaseConnection.getDatabaseConnection();
            statement = connection.createStatement();
            String getUser = "select * from User where UserName = '" + userLoginDto.getUserName() + "' and Status = 1";
            userResult = statement.executeQuery(getUser);

            if (userResult.next() == false) {
                System.out.println("username not found");
            } else {
                try {

                    String decryptedPassword = PasswordEncoderDecoder.getInstance()
                            .decrypt(userResult.getString(ApplicationConstants.USER_PASSWORD_COLUMN));
                    if (decryptedPassword.equals(userLoginDto.getPassword())) {
                        userLoginResponseModel.setRoleId(userResult.getString(ApplicationConstants.USER_ROLEID_COLUMN));
                        userLoginResponseModel
                                .setUserName(userResult.getString(ApplicationConstants.USER_USERNAME_COLUMN));
                        userLoginResponseModel.setUserId(userResult.getString(ApplicationConstants.USER_USERID_COLUMN));
                        String loggedInUserRoleId = userResult.getString(ApplicationConstants.USER_ROLEID_COLUMN);
                        String loggedInUserUserId = userResult.getString(ApplicationConstants.USER_USERID_COLUMN);
                        String loggedInUserUserName = userResult.getString(ApplicationConstants.USER_USERNAME_COLUMN);
                        userLoginResponseModel.setRoleId(loggedInUserRoleId);
                        userLoginResponseModel.setUserId(loggedInUserUserId);
                        userLoginResponseModel.setUserName(loggedInUserUserName);

                        if (loggedInUserRoleId.equals(ApplicationConstants.RESTAURANT_OWNER_ROLEID)) {
                            getResturantIdAndName(loggedInUserUserId, userLoginResponseModel);
                        }
                        return userLoginResponseModel;
                    } else {
                        return userLoginResponseModel;
                    }
                } catch (Exception ex) {
                    System.out.println("Exception: In Password Encoding In User Registration");
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception : UserRepository - loginUser()");
            System.out.println(ex);
            System.out.println(ex.getMessage());
        } finally {
            try {
                connection.close();
                statement.close();
                userResult.close();
            } catch (Exception ex) {
                System.out.println("Exception : UserRepository - Closing database connection in registerUser()");
            }
        }
        return userLoginResponseModel;
    }

    public void getResturantIdAndName(String userId, UserLoginResponseModel userLoginResponseModel) {
        try {
            String getUser = "select * from Restaurant where User_UserId = '" + userId + "' and Status = 1";
            userResult = statement.executeQuery(getUser);
            userResult.next();
            try {
                userLoginResponseModel.setRestaurantId(userResult.getString("RestaurantId"));
            } catch (Exception ex) {
                System.out.println(ex);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public UserDetailsDto getUsersDTOFromResultSet(ResultSet resultSet) {
        UserDetailsDto userDetailsDto = new UserDetailsDto();

        try {
            while (resultSet.next()) {
                userDetailsDto.setId(Integer.parseInt(resultSet.getString("Id")));
                userDetailsDto.setUserId(resultSet.getString("UserId"));
                userDetailsDto.setFirstName(resultSet.getString("FirstName"));
                userDetailsDto.setLastName(resultSet.getString("LastName"));
                userDetailsDto.setEmailId(resultSet.getString("EmailId"));
                userDetailsDto.setUserName(resultSet.getString("UserName"));
                userDetailsDto.setPassword(resultSet.getString("Password"));
                userDetailsDto.setGender(resultSet.getString("Gender"));

                userDetailsDto.setBirthDate(resultSet.getDate("BirthDate"));
                userDetailsDto.setAddress(resultSet.getString("Address"));
                userDetailsDto.setCity(resultSet.getString("City"));
                userDetailsDto.setProvince(resultSet.getString("Province"));
                userDetailsDto.setCountry(resultSet.getString("Country"));
                userDetailsDto.setPostalCode(resultSet.getString("PostalCode"));
                userDetailsDto.setStatus(resultSet.getInt("Status"));
                userDetailsDto.setRoleId(resultSet.getString("Role_RoleId"));
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return userDetailsDto;
    }

    @Override
    public UserDetailsDto getUserDetails(String id) {
        UserDetailsDto userDetailsDto = null;
        ResultSet resultSet = null;
        try {

            connection = databaseConnection.getDatabaseConnection();
            statement = connection.createStatement();

            String getUser = "select * from User where UserId = '" + id + "'";
            resultSet = statement.executeQuery(getUser);

            userDetailsDto = getUsersDTOFromResultSet(resultSet);

        } catch (Exception ex) {
            System.out.println(ex);

        } finally {
            try {
                connection.close();
                statement.close();
                resultSet.close();
            } catch (Exception ex) {
                System.out.println("Exception : UserRepository - Closing database connection in registerUser()");
            }
        }
        return userDetailsDto;

    }

    @Override
    public void updateUserProfile(UserDetailsDto userDetails) {
        Connection connection;

        try {
            connection = databaseConnection.getDatabaseConnection();

            String query = "update User SET  UserId=?, FirstName=?, LastName=?, EmailId=?, UserName=?,  MobileNumber=?, Gender=?, BirthDate=?, Address=?, City=?, Province=?, Country=?, PostalCode=?  where UserId= '"
                    + userDetails.getUserId() + "'";

            PreparedStatement prepStat = connection.prepareStatement(query);

            prepStat.setString(1, userDetails.getUserId());
            prepStat.setString(2, userDetails.getFirstName());
            prepStat.setString(3, userDetails.getLastName());
            prepStat.setString(4, userDetails.getEmailId());
            prepStat.setString(5, userDetails.getUserName());
            prepStat.setString(6, userDetails.getMobileNumber());
            prepStat.setString(7, userDetails.getGender());
            prepStat.setDate(8, userDetails.getBirthDate());
            prepStat.setString(9, userDetails.getAddress());
            prepStat.setString(10, userDetails.getCity());
            prepStat.setString(11, userDetails.getProvince());
            prepStat.setString(12, userDetails.getCountry());
            prepStat.setString(13, userDetails.getPostalCode());

            prepStat.executeUpdate();

            connection.close();

        } catch (Exception e) {
            System.out.println(e);

        }
    }
}
