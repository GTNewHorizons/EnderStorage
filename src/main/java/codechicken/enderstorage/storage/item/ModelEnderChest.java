package codechicken.enderstorage.storage.item;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.Vertex5;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.RedundantTransformation;
import codechicken.lib.vec.Vector3;

public class ModelEnderChest {

    private static final Vector3 X = new Vector3(1, 0, 0);
    private static final Matrix4 lidMat = new Matrix4();
    private static final CCRenderState.IVertexOperation[] NO_OPS = new CCRenderState.IVertexOperation[] {
            new RedundantTransformation() };
    private static final CCRenderState.IVertexOperation[] LID_OPS = new CCRenderState.IVertexOperation[] { lidMat };

    private static final CCModel baseModel = bakeBox(1, 6, 1, 15, 16, 15, 0, 19);
    private static final CCModel lidModel = bakeBox(1, 2, 1, 15, 7, 15, 0, 0);
    private static final CCModel knobModel = bakeBox(7, 5, 0, 9, 9, 1, 0, 0);
    private static final CCModel diamondKnobModel = bakeBox(7, 5, 0, 9, 9, 1, 0, 5);

    private ModelEnderChest() {}

    public static void render(CCRenderState state, boolean personal, float lidAngle) {
        lidMat.setIdentity().translate(0.0625, 0.4375, 0.9375).rotate(lidAngle, X).translate(-0.0625, -0.4375, -0.9375);
        state.startDrawingInstance(7);
        renderModel(state, baseModel, NO_OPS);
        renderModel(state, lidModel, LID_OPS);
        if (personal) renderModel(state, diamondKnobModel, LID_OPS);
        else renderModel(state, knobModel, LID_OPS);
        state.drawInstance();
    }

    private static void renderModel(CCRenderState state, CCModel model, CCRenderState.IVertexOperation[] ops) {
        // ops must never be empty: CCRenderPipeline.rebuild() returns early on an empty list, skipping the
        // standard normal/colour operations and leaving the batch without per-vertex normals
        model.render(state, 0, model.verts.length, ops);
    }

    /**
     * Bakes a box with the exact vertex order and UV layout of a vanilla ModelRenderer box, using coordinates already
     * offset into the model's parent space (rotation point baked in). Positions are in texture pixels, divided by 16.
     */
    private static CCModel bakeBox(double x1, double y1, double z1, double x2, double y2, double z2, double tx,
            double ty) {
        double w = x2 - x1;
        double h = y2 - y1;
        double d = z2 - z1;
        CCModel model = CCModel.quadModel(24);
        Vertex5[] verts = model.verts;
        int i = 0;
        i = face(
                verts,
                i,
                x2,
                y1,
                z2,
                x2,
                y1,
                z1,
                x2,
                y2,
                z1,
                x2,
                y2,
                z2,
                tx + d + w,
                ty + d,
                tx + 2 * d + w,
                ty + d + h);
        i = face(verts, i, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, tx, ty + d, tx + d, ty + d + h);
        i = face(verts, i, x2, y1, z2, x1, y1, z2, x1, y1, z1, x2, y1, z1, tx + d, ty, tx + d + w, ty + d);
        i = face(verts, i, x2, y2, z1, x1, y2, z1, x1, y2, z2, x2, y2, z2, tx + d + w, ty + d, tx + 2 * d + w, ty);
        i = face(verts, i, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, tx + d, ty + d, tx + d + w, ty + d + h);
        face(
                verts,
                i,
                x1,
                y1,
                z2,
                x2,
                y1,
                z2,
                x2,
                y2,
                z2,
                x1,
                y2,
                z2,
                tx + d + w + d,
                ty + d,
                tx + 2 * d + 2 * w,
                ty + d + h);
        return model.computeNormals();
    }

    private static int face(Vertex5[] verts, int i, double ax, double ay, double az, double bx, double by, double bz,
            double cx, double cy, double cz, double dx, double dy, double dz, double u1, double v1, double u2,
            double v2) {
        verts[i] = new Vertex5(ax / 16, ay / 16, az / 16, u2 / 64, v1 / 64);
        verts[i + 1] = new Vertex5(bx / 16, by / 16, bz / 16, u1 / 64, v1 / 64);
        verts[i + 2] = new Vertex5(cx / 16, cy / 16, cz / 16, u1 / 64, v2 / 64);
        verts[i + 3] = new Vertex5(dx / 16, dy / 16, dz / 16, u2 / 64, v2 / 64);
        return i + 4;
    }
}
