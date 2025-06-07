package quilt.ghgen.findissueid

import com.expediagroup.graphql.client.Generated
import kotlinx.serialization.Serializable
import quilt.ghgen.ID

/**
 * A repository pull request.
 */
@Generated
@Serializable
public data class PullRequest(
  public val id: ID,
)
