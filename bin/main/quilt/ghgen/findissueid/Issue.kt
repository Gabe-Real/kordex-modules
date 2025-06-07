package quilt.ghgen.findissueid

import com.expediagroup.graphql.client.Generated
import kotlin.String
import kotlinx.serialization.Serializable
import quilt.ghgen.ID

/**
 * An Issue is a place to discuss ideas, enhancements, tasks, and bugs for a project.
 */
@Generated
@Serializable
public data class Issue(
  /**
   * Identifies the issue title.
   */
  public val title: String,
  /**
   * The actor who authored the comment.
   */
  public val author: Actor? = null,
  /**
   * Identifies the body of the issue.
   */
  public val body: String,
  public val id: ID,
)
