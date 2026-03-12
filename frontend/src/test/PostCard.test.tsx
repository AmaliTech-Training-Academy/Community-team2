import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { PostCard } from "../components/molecules/PostCard";
import { TEST_IDS } from "../utils/TEST_IDS";
import type { Post } from "../types";

const mockPost: Post = {
  id: 42,
  title: "Test Post Title",
  body: "This is the body of the post for testing purposes.",
  category: "Events",
  author: "Jane Doe",
  authorId: 1,
  createdAt: new Date(Date.now() - 60_000).toISOString(),
  comments: [
    {
      id: 1,
      text: "hi",
      author: "Bob",
      authorId: 2,
      createdAt: new Date().toISOString(),
    },
  ],
};

describe("PostCard", () => {
  it("renders the dynamic card test id", () => {
    render(<PostCard post={mockPost} onClick={vi.fn()} />);

    expect(
      screen.getByTestId(TEST_IDS.POST_CARD.CARD(mockPost.id)),
    ).toBeInTheDocument();
  });

  it("shows the post title", () => {
    render(<PostCard post={mockPost} onClick={vi.fn()} />);

    expect(screen.getByTestId(TEST_IDS.POST_CARD.TITLE)).toHaveTextContent(
      mockPost.title,
    );
  });

  it("shows the post body", () => {
    render(<PostCard post={mockPost} onClick={vi.fn()} />);

    expect(screen.getByTestId(TEST_IDS.POST_CARD.EXCERPT)).toHaveTextContent(
      mockPost.body,
    );
  });

  it("shows the author name", () => {
    render(<PostCard post={mockPost} onClick={vi.fn()} />);

    expect(screen.getByTestId(TEST_IDS.POST_CARD.AUTHOR)).toHaveTextContent(
      mockPost.author,
    );
  });

  it("shows the comment count", () => {
    render(<PostCard post={mockPost} onClick={vi.fn()} />);

    expect(
      screen.getByTestId(TEST_IDS.POST_CARD.COMMENT_COUNT),
    ).toHaveTextContent("1");
  });

  it("does not render the image when imageUrl is absent", () => {
    render(<PostCard post={mockPost} onClick={vi.fn()} />);

    expect(screen.queryByTestId("post-card-image")).not.toBeInTheDocument();
    expect(screen.queryByTestId("post-card-has-image")).not.toBeInTheDocument();
  });

  it("shows only the image indicator when imageUrl is present", () => {
    render(
      <PostCard
        post={{ ...mockPost, imageUrl: "https://example.com/image.jpg" }}
        onClick={vi.fn()}
      />,
    );

    expect(screen.queryByTestId("post-card-image")).not.toBeInTheDocument();
    expect(screen.getByTestId("post-card-has-image")).toBeInTheDocument();
  });

  it("calls onClick when the card is clicked", async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();
    render(<PostCard post={mockPost} onClick={onClick} />);

    await user.click(screen.getByTestId(TEST_IDS.POST_CARD.CARD(mockPost.id)));

    expect(onClick).toHaveBeenCalledTimes(1);
  });
});
