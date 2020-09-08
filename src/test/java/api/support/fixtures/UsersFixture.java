package api.support.fixtures;

import static api.support.fixtures.UserExamples.basedUponBobbyBibbin;
import static api.support.fixtures.UserExamples.basedUponCharlotteBroadwell;
import static api.support.fixtures.UserExamples.basedUponHenryHanks;
import static api.support.fixtures.UserExamples.basedUponJamesRodwell;
import static api.support.fixtures.UserExamples.basedUponJessicaPontefract;
import static api.support.fixtures.UserExamples.basedUponRebeccaStuart;
import static api.support.fixtures.UserExamples.basedUponStevenJones;
import static java.util.function.Function.identity;
import static org.folio.circulation.support.JsonPropertyFetcher.getProperty;

import java.util.function.Function;

import org.folio.circulation.support.http.client.IndividualResource;

import api.support.APITestContext;
import api.support.builders.UserBuilder;
import api.support.http.ResourceClient;
import api.support.resources.UserResource;

public class UsersFixture {
  private final RecordCreator userRecordCreator;
  private final PatronGroupsFixture patronGroupsFixture;

  public UsersFixture(ResourceClient usersClient, PatronGroupsFixture patronGroupsFixture) {
    this.userRecordCreator = new RecordCreator(usersClient,
      user -> getProperty(user, "username"));

    this.patronGroupsFixture = patronGroupsFixture;
  }

  public UserResource jessica() {
    return createUser(basedUponJessicaPontefract()
        .inGroupFor(patronGroupsFixture.regular()));
  }

  public UserResource james() {
    return createUser(basedUponJamesRodwell()
      .inGroupFor(patronGroupsFixture.regular()));
  }

  public UserResource rebecca() {
    return rebecca(identity());
  }

  public UserResource rebecca(Function<UserBuilder, UserBuilder> additionalProperties) {
    return createUser(additionalProperties.apply(basedUponRebeccaStuart()
      .inGroupFor(patronGroupsFixture.regular())));
  }

  public UserResource steve() {
    return steve(identity());
  }

  public UserResource steve(Function<UserBuilder, UserBuilder> additionalUserProperties) {
    return createUser(additionalUserProperties.apply(basedUponStevenJones()
      .inGroupFor(patronGroupsFixture.regular())));
  }

  public UserResource charlotte() {
    return charlotte(identity());
  }

  public UserResource charlotte(Function<UserBuilder, UserBuilder> additionalConfiguration) {
    return createUser(additionalConfiguration.apply(basedUponCharlotteBroadwell()
      .inGroupFor(patronGroupsFixture.regular())));
  }

  public UserResource undergradHenry() {
    return undergradHenry(identity());
  }

  public UserResource undergradHenry(Function<UserBuilder, UserBuilder> additionalConfiguration) {
    return createUser(additionalConfiguration.apply(basedUponHenryHanks()
      .inGroupFor(patronGroupsFixture.undergrad())));
  }

  public UserResource noUserGroupBob() {
    return noUserGroupBob(identity());
  }

  public UserResource noUserGroupBob(Function<UserBuilder, UserBuilder> additionalConfiguration) {
    return createUser(additionalConfiguration.apply(basedUponBobbyBibbin()));
  }

  public void cleanUp() {
    userRecordCreator.cleanUp();
  }

  public void remove(IndividualResource user) {
    userRecordCreator.delete(user);
  }

  public void defaultAdmin() {
    createUser(new UserBuilder()
      .withName("Admin", "Admin")
      .withNoBarcode()
      .withId(APITestContext.getUserId()));
  }

  private UserResource createUser(UserBuilder builder) {
    return new UserResource(userRecordCreator.createIfAbsent(builder));
  }
}
