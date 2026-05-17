const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.firestore();

exports.onOrderCreated = onDocumentCreated("orders/{orderId}", async (event) => {
  const orderId = event.params.orderId;
  const shortId = orderId.slice(-5).toUpperCase();

  const workersSnapshot = await db.collection("users")
    .where("role", "==", "worker")
    .get();

  const tokens = workersSnapshot.docs
    .map((doc) => doc.data().fcmToken)
    .filter((token) => token && token.length > 0);

  if (tokens.length === 0) return null;

  return admin.messaging().sendEachForMulticast({
    tokens: tokens,
    notification: {
      title: "Nueva orden recibida",
      body: `Orden #${shortId} está esperando preparación.`,
    },
    data: {
      orderId: orderId,
      type: "pending",
    },
    android: {
      channelId: "comal_orders",
      priority: "high",
    },
  });
});

exports.onOrderStatusChanged = onDocumentUpdated("orders/{orderId}", async (event) => {
  const before = event.data.before.data();
  const after = event.data.after.data();

  if (before.status === after.status) return null;

  const orderId = event.params.orderId;
  const shortId = orderId.slice(-5).toUpperCase();
  const userId = after.userId;

  const userDoc = await db.collection("users").doc(userId).get();
  if (!userDoc.exists) return null;

  const fcmToken = userDoc.data().fcmToken;
  if (!fcmToken) return null;

  const notifications = {
    preparing: {
      title: "Orden en preparación",
      body: `Tu orden #${shortId} está siendo preparada por el equipo.`,
    },
    ready: {
      title: "¡Tu orden está lista!",
      body: `Tu orden #${shortId} está lista para recoger en el mostrador.`,
    },
    delivered: {
      title: "Orden entregada",
      body: `Tu orden #${shortId} fue entregada. ¡Buen provecho!`,
    },
    cancelled: {
      title: "Orden cancelada",
      body: `Tu orden #${shortId} ha sido cancelada.`,
    },
  };

  const notification = notifications[after.status];
  if (!notification) return null;

  return admin.messaging().send({
    token: fcmToken,
    notification: {
      title: notification.title,
      body: notification.body,
    },
    data: {
      orderId: orderId,
      type: after.status,
    },
    android: {
      channelId: "comal_orders",
      priority: "high",
    },
  });
});