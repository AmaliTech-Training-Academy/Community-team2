/**
 * ─────────────────────────────────────────────────────────────────────────────
 *  PING — Complete Mock Data
 *
 *  This file is the single source of truth for all in-code mock data.
 *  It mirrors every JSON payload that the real Spring Boot backend returns
 *  so you can run the frontend with zero backend dependency.
 *
 *  It is also the exact data you paste into MockAPI.io (see MOCKAPI_GUIDE.md).
 * ─────────────────────────────────────────────────────────────────────────────
 */

import type { User, Post, Comment, Analytics, AuthResponse } from '../types';

// ── Users ─────────────────────────────────────────────────────────────────────
// Two seed accounts the mock login accepts (see mockAuthApi.ts for credentials).
export const MOCK_USERS: User[] = [
  {
    id:    1,
    email: 'admin@ping.com',
    name:  'Alex Morgan',
    role:  'ADMIN',
  },
  {
    id:    2,
    email: 'user@ping.com',
    name:  'Jordan Lee',
    role:  'USER',
  },
  {
    id:    3,
    email: 'kwame@ping.com',
    name:  'Kwame Asante',
    role:  'USER',
  },
  {
    id:    4,
    email: 'priya@ping.com',
    name:  'Priya Nair',
    role:  'USER',
  },
  {
    id:    5,
    email: 'sofia@ping.com',
    name:  'Sofia Reyes',
    role:  'USER',
  },
];

// ── Comments (reusable across posts) ─────────────────────────────────────────
const makeComments = (postId: number, pairs: Array<[number, string, string]>): Comment[] =>
  pairs.map(([id, author, text], i) => ({
    id,
    text,
    author,
    authorId: MOCK_USERS.find(u => u.name === author)?.id ?? 2,
    createdAt: new Date(Date.now() - (postId * 3 + i) * 3_600_000).toISOString(),
  }));

// ── Posts ─────────────────────────────────────────────────────────────────────
export const MOCK_POSTS: Post[] = [
  {
    id:       1,
    title:    'Summer Block Party — Save the Date!',
    body:     'Hey neighbors! We are organizing a summer block party on July 20th from 3 PM to 9 PM on Maple Avenue. There will be food stalls, live music, games for the kids, and a fireworks display at dusk. Volunteers needed for setup from 12 PM. Reply here or DM me if you can help!',
    category: 'Events',
    author:   'Alex Morgan',
    authorId: 1,
    createdAt: new Date(Date.now() - 2 * 86_400_000).toISOString(),
    comments: makeComments(1, [
      [101, 'Jordan Lee',   'This sounds amazing! Count me in for volunteering.'],
      [102, 'Kwame Asante', 'Will there be vegetarian food options?'],
      [103, 'Priya Nair',   'I can bring my portable speaker setup for the music corner!'],
    ]),
  },
  {
    id:       2,
    title:    'Lost: Golden Retriever named Biscuit',
    body:     'Our golden retriever Biscuit went missing yesterday evening around Pine Street and Oak Avenue. He is 3 years old, wearing a red collar with tags. Very friendly, responds to his name. If you spot him please call 555-0192 or message me here immediately. Reward offered.',
    category: 'Lost & Found',
    author:   'Priya Nair',
    authorId: 4,
    createdAt: new Date(Date.now() - 18 * 3_600_000).toISOString(),
    comments: makeComments(2, [
      [104, 'Sofia Reyes',  'Saw a dog matching this description near the park this morning! Will keep an eye out.'],
      [105, 'Alex Morgan',  'Sharing this on the community board. Hope you find him soon!'],
    ]),
  },
  {
    id:       3,
    title:    'Best Coffee Shops in the Neighborhood — My Top 5',
    body:     '1. Grounds & Glory on Elm St — best flat white in town. 2. The Bean Scene — amazing pastries and fast wifi. 3. Roast & Co — cozy corners, great for working. 4. Morning Ritual — locally sourced beans. 5. The Daily Grind — cheapest espresso, open until midnight. All within walking distance!',
    category: 'Recommendations',
    author:   'Jordan Lee',
    authorId: 2,
    createdAt: new Date(Date.now() - 4 * 86_400_000).toISOString(),
    comments: makeComments(3, [
      [106, 'Kwame Asante', 'Grounds & Glory is my go-to! Their oat milk latte is 🔥'],
      [107, 'Priya Nair',   'Adding The Bean Scene to my list, thanks!'],
      [108, 'Sofia Reyes',  'Morning Ritual just started a loyalty card scheme too.'],
    ]),
  },
  {
    id:       4,
    title:    'Need Help Moving a Couch this Weekend',
    body:     'Hi all, I am moving apartments this Saturday and my new sofa is too heavy for one person. Looking for 2–3 able-bodied volunteers to help carry it up 3 flights of stairs. Should take under an hour. I will provide pizza, drinks, and my eternal gratitude. Let me know in the comments!',
    category: 'Help Requests',
    author:   'Kwame Asante',
    authorId: 3,
    createdAt: new Date(Date.now() - 1 * 86_400_000).toISOString(),
    comments: makeComments(4, [
      [109, 'Jordan Lee', 'I can help! What time on Saturday?'],
      [110, 'Alex Morgan', 'Count me in too. DM me the address.'],
    ]),
  },
  {
    id:       5,
    title:    'New Library Branch Opening Next Month',
    body:     'Great news for book lovers! The city council has confirmed that the new Westside branch of the Public Library will open on August 1st. It will feature a digital media lab, co-working spaces, a children\'s reading room, and an expanded local history archive. Opening ceremony at 10 AM — all welcome.',
    category: 'News',
    author:   'Alex Morgan',
    authorId: 1,
    createdAt: new Date(Date.now() - 6 * 86_400_000).toISOString(),
    comments: makeComments(5, [
      [111, 'Priya Nair',   'Finally! I\'ve been waiting for this for years.'],
      [112, 'Sofia Reyes',  'Will there be evening hours? I work days.'],
      [113, 'Kwame Asante', 'The digital media lab sounds incredible. Any details on what equipment they\'ll have?'],
    ]),
  },
  {
    id:       6,
    title:    'Neighborhood Clean-Up Day — Saturday 8 AM',
    body:     'Join us this Saturday from 8 AM to 12 PM for our monthly neighborhood clean-up! We will be focusing on the riverside trail and the park near the community center. Gloves and bags provided. Bring sunscreen and sturdy shoes. Great activity for families — kids welcome!',
    category: 'Events',
    author:   'Sofia Reyes',
    authorId: 5,
    createdAt: new Date(Date.now() - 3 * 86_400_000).toISOString(),
    comments: makeComments(6, [
      [114, 'Jordan Lee',   'My family will be there!'],
      [115, 'Alex Morgan',  'I can bring extra trash bags if needed.'],
    ]),
  },
  {
    id:       7,
    title:    'Found: Set of Keys near the Post Office',
    body:     'Found a set of 4 keys on a blue lanyard outside the main post office on Center Street this afternoon. There is a small Eiffel Tower keychain attached. If these are yours, DM me with a description of the keys to verify ownership and I will arrange to return them.',
    category: 'Lost & Found',
    author:   'Jordan Lee',
    authorId: 2,
    createdAt: new Date(Date.now() - 12 * 3_600_000).toISOString(),
    comments: makeComments(7, [
      [116, 'Sofia Reyes', 'Good on you for posting this!'],
    ]),
  },
  {
    id:       8,
    title:    'Best Plumber in the Area — Recommendations Needed',
    body:     'My kitchen sink has been leaking for a week and I need a reliable plumber urgently. Previous one I hired was unreliable and overpriced. Looking for personal recommendations from neighbors — someone honest, affordable, and available this week. Please share names and contact details!',
    category: 'Help Requests',
    author:   'Priya Nair',
    authorId: 4,
    createdAt: new Date(Date.now() - 8 * 3_600_000).toISOString(),
    comments: makeComments(8, [
      [117, 'Kwame Asante', 'Mike from FixIt Fast on Grove St — call 555-0134. Very reliable, fair prices.'],
      [118, 'Alex Morgan',  'Seconding Mike! Fixed our boiler last winter, no issues since.'],
    ]),
  },
  {
    id:       9,
    title:    'Road Works on Main Street — Expect Delays',
    body:     'Heads up to all drivers and cyclists: the city has scheduled water pipe replacement works on Main Street between Oak Ave and Cedar Lane starting Monday. Works expected to last 3 weeks. Use Birch Road as an alternative route. Bus routes 14 and 22 will be diverted — check the transit website for updated stops.',
    category: 'News',
    author:   'Alex Morgan',
    authorId: 1,
    createdAt: new Date(Date.now() - 5 * 86_400_000).toISOString(),
    comments: makeComments(9, [
      [119, 'Jordan Lee',   'Thanks for the heads up! Birch Road will be packed.'],
      [120, 'Sofia Reyes',  'This is going to be a nightmare for the school run.'],
      [121, 'Priya Nair',   'Bus 14 is already unreliable without diversions 😭'],
    ]),
  },
  {
    id:       10,
    title:    'Highly Recommend: New Thai Restaurant on Vine St',
    body:     'Just tried "Orchid Thai" that opened last week on Vine Street and it is absolutely incredible. The pad see ew is the best I\'ve had outside of Bangkok. Very reasonable prices (mains around $12–16), generous portions, and the service was warm and attentive. Definitely worth a visit. They do takeaway too!',
    category: 'Recommendations',
    author:   'Kwame Asante',
    authorId: 3,
    createdAt: new Date(Date.now() - 2 * 86_400_000).toISOString(),
    comments: makeComments(10, [
      [122, 'Priya Nair',  'Went last night after seeing this — the green curry was amazing!'],
      [123, 'Jordan Lee',  'Do they have vegan options?'],
      [124, 'Kwame Asante', 'Yes! Their tofu dishes are excellent too.'],
    ]),
  },
];

// ── Analytics ─────────────────────────────────────────────────────────────────
const last30Days = (): { date: string; count: number }[] => {
  const days: { date: string; count: number }[] = [];
  for (let i = 29; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    days.push({
      date:  d.toISOString().slice(0, 10),
      count: Math.floor(Math.random() * 4),
    });
  }
  // Seed a few spikes so the line chart looks interesting
  days[2].count  = 5;
  days[8].count  = 7;
  days[14].count = 4;
  days[21].count = 6;
  days[27].count = 3;
  return days;
};

export const MOCK_ANALYTICS: Analytics = {
  totalPosts:  MOCK_POSTS.length,
  totalUsers:  MOCK_USERS.length,
  categoryBreakdown: {
    'Events':          2,
    'Lost & Found':    2,
    'Recommendations': 2,
    'Help Requests':   2,
    'News':            2,
  },
  dayActivity: [
    { day: 'Mon', count: 3 },
    { day: 'Tue', count: 5 },
    { day: 'Wed', count: 2 },
    { day: 'Thu', count: 7 },
    { day: 'Fri', count: 4 },
    { day: 'Sat', count: 6 },
    { day: 'Sun', count: 1 },
  ],
  postTrend: last30Days(),
  topContributors: [
    { name: 'Alex Morgan',  count: 3 },
    { name: 'Jordan Lee',   count: 2 },
    { name: 'Kwame Asante', count: 2 },
    { name: 'Priya Nair',   count: 2 },
    { name: 'Sofia Reyes',  count: 1 },
  ],
};

// ── Auth responses (mock login tokens) ───────────────────────────────────────
// Real JWTs have 3 base64url parts.  These are fake but correctly structured
// so our decodeJwt() utility parses them without throwing.
// header.payload.signature  — payload is base64url({ sub, name, email, role, exp })

export function makeFakeJwt(user: User): string {
  const header  = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' })).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
  const payload = btoa(JSON.stringify({
    sub:   String(user.id),
    id:    user.id,
    name:  user.name,
    email: user.email,
    role:  user.role,
    iat:   Math.floor(Date.now() / 1000),
    exp:   Math.floor(Date.now() / 1000) + 86_400, // 24-hour expiry
  })).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
  const sig = 'mock_signature_not_verified';
  return `${header}.${payload}.${sig}`;
}

export const MOCK_AUTH_RESPONSES: Record<string, AuthResponse & { user: User }> = {
  'admin@ping.com': { token: makeFakeJwt(MOCK_USERS[0]), user: MOCK_USERS[0] },
  'user@ping.com':  { token: makeFakeJwt(MOCK_USERS[1]), user: MOCK_USERS[1] },
  'kwame@ping.com': { token: makeFakeJwt(MOCK_USERS[2]), user: MOCK_USERS[2] },
  'priya@ping.com': { token: makeFakeJwt(MOCK_USERS[3]), user: MOCK_USERS[3] },
  'sofia@ping.com': { token: makeFakeJwt(MOCK_USERS[4]), user: MOCK_USERS[4] },
};
