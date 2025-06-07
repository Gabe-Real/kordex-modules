package quilt.ghgen.findissueid

import com.expediagroup.graphql.client.Generated
import kotlinx.serialization.Serializable

/**
 * A repository contains the content for a project.
 */
@Generated
@Serializable
public data class Repository(
  /**
   * Returns a single issue from the current repository by number.
   */
  public val issue: Issue? = null,
  /**
   * Returns a single pull request from the current repository by number.
   */
  public val pullRequest: PullRequest? = null,
)
