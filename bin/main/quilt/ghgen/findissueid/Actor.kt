package quilt.ghgen.findissueid

import com.expediagroup.graphql.client.Generated
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import quilt.ghgen.ID

/**
 * Represents an object which can take actions on GitHub. Typically a User or Bot.
 */
@Generated
@Serializable
public sealed class Actor

/**
 * An account for a user who is an admin of an enterprise or a member of an enterprise through one
 * or more organizations.
 */
@Generated
@Serializable
@SerialName(value = "EnterpriseUserAccount")
public data class EnterpriseUserAccount(
  /**
   * An identifier for the enterprise user account, a login or email address
   */
  public val login: String,
  public val id: ID,
) : Actor()

/**
 * An account on GitHub, with one or more owners, that has repositories, members and teams.
 */
@Generated
@Serializable
@SerialName(value = "Organization")
public data class Organization(
  /**
   * The organization's login name.
   */
  public val login: String,
  public val id: ID,
) : Actor()

/**
 * A special type of user which takes actions on behalf of GitHub Apps.
 */
@Generated
@Serializable
@SerialName(value = "Bot")
public data class Bot(
  /**
   * The username of the actor.
   */
  public val login: String,
  public val id: ID,
) : Actor()

/**
 * A placeholder user for attribution of imported data on GitHub.
 */
@Generated
@Serializable
@SerialName(value = "Mannequin")
public data class Mannequin(
  /**
   * The username of the actor.
   */
  public val login: String,
  public val id: ID,
) : Actor()

/**
 * A user is an individual's account on GitHub that owns repositories and can make new content.
 */
@Generated
@Serializable
@SerialName(value = "User")
public data class User(
  /**
   * The username used to login.
   */
  public val login: String,
  public val id: ID,
) : Actor()

/**
 * Fallback Actor implementation that will be used when unknown/unhandled type is encountered.
 *
 * NOTE: This fallback logic has to be manually registered with the instance of
 * GraphQLClientKotlinxSerializer. See documentation for details.
 */
@Generated
@Serializable
public class DefaultActorImplementation() : Actor()
