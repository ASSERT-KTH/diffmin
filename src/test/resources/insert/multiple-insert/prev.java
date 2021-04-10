import java.util.Queue;

class TreeTraversal {

    class TreeNode {
        public int val;
        public TreeNode left;
        public TreeNode right;

        public TreeNode(int val) {
            this.val = val;
        }
    }

    public void levelOrder(TreeNode root) {
        Queue<TreeNode> q;
        q.add(root);

        while (!q.isEmpty()) {
            int size = q.size();

            for (int i=0; i<n; ++i) {
                TreeNode p = q.peek();
                q.poll();

                if (p.left != null) {
                    q.add(p.left);
                }
            }
        }
    }

    public void inOrder(TreeNode root) {
        inOrder(root.left);
        System.out.println(root.val);
        inOrder(root.right);
    }
}
